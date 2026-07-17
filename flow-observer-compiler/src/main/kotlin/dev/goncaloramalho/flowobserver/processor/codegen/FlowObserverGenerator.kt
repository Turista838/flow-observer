package dev.goncaloramalho.flowobserver.processor.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import dev.goncaloramalho.flowobserver.processor.model.FlowKind
import dev.goncaloramalho.flowobserver.processor.model.FlowObserverPlan
import dev.goncaloramalho.flowobserver.processor.model.LoggedFlow
import dev.goncaloramalho.flowobserver.processor.model.ViewModelLoggingPlan
import dev.goncaloramalho.flowobserver.processor.util.Fqns

internal class FlowObserverGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    fun generate(plan: FlowObserverPlan, originatingFiles: Array<KSFile>) {
        for (viewModelPlan in plan.viewModels) {
            generateViewModelExtensions(viewModelPlan, originatingFiles)
        }
    }

    private fun generateViewModelExtensions(
        plan: ViewModelLoggingPlan,
        originatingFiles: Array<KSFile>,
    ) {
        val flowBlocks = plan.flows.joinToString("\n\n") { generateFlowCollector(it) }
        val fileName = "${plan.className}_FlowObserverGenerated"

        writeFile(
            packageName = plan.packageName,
            fileName = fileName,
            originatingFiles = originatingFiles,
            aggregating = false,
            content = """
                |package ${plan.packageName}
                |
                |import android.util.Log
                |import androidx.lifecycle.viewModelScope
                |import ${Fqns.FLOW_OBSERVER}
                |import kotlinx.coroutines.flow.drop
                |import kotlinx.coroutines.flow.launchIn
                |import kotlinx.coroutines.flow.onEach
                |
                |fun ${plan.className}.attachFlowObserver() {
                |$flowBlocks
                |}
            """.trimMargin(),
        )

        logger.warn("Generated flow observer extensions: ${plan.packageName}.$fileName")
    }

    private fun generateFlowCollector(flow: LoggedFlow): String {
        return when (flow.flowKind) {
            FlowKind.STATE_FLOW -> generateStateFlowCollector(flow)
            FlowKind.SHARED_FLOW -> generateSharedFlowCollector(flow)
        }
    }

    private fun generateStateFlowCollector(flow: LoggedFlow): String {
        val previousVar = previousVarName(flow.propertyName)
        val nextVar = nextVarName(flow.propertyName)
        val tag = escape(flow.tag)
        return """
            |    var $previousVar = ${flow.propertyName}.value
            |    ${flow.propertyName}
            |        .drop(1)
            |        .onEach { $nextVar ->
            |            if (FlowObserver.settings.enabled) {
            |                val message = "change { previousState: ${'$'}$previousVar, currentState: ${'$'}$nextVar }"
            |                val logger = FlowObserver.settings.logger
            |                if (logger != null) {
            |                    logger.log("$tag", message)
            |                } else {
            |                    Log.i("$tag", message)
            |                }
            |            }
            |            $previousVar = $nextVar
            |        }
            |        .launchIn(viewModelScope)
        """.trimMargin()
    }

    private fun generateSharedFlowCollector(flow: LoggedFlow): String {
        val tag = escape(flow.tag)

        return """
            |    ${flow.propertyName}
            |        .onEach { value ->
            |            if (!FlowObserver.settings.enabled) return@onEach
            |            val message = "event { ${'$'}value }"
            |            val logger = FlowObserver.settings.logger
            |            if (logger != null) {
            |                logger.log("$tag", message)
            |            } else {
            |                Log.i("$tag", message)
            |            }
            |        }
            |        .launchIn(viewModelScope)
        """.trimMargin()
    }

    private fun previousVarName(propertyName: String): String =
        "previous${propertyName.replaceFirstChar { char -> char.uppercaseChar() }}"

    private fun nextVarName(propertyName: String): String =
        "next${propertyName.replaceFirstChar { char -> char.uppercaseChar() }}"

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun writeFile(
        packageName: String,
        fileName: String,
        originatingFiles: Array<KSFile>,
        aggregating: Boolean,
        content: String,
    ) {
        codeGenerator.createNewFile(
            Dependencies(aggregating = aggregating, sources = originatingFiles),
            packageName,
            fileName,
            "kt",
        ).writer().use { it.write(content) }
    }
}
