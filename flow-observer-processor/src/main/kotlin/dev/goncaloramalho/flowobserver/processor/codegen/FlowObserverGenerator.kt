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
        generateMaster(plan, originatingFiles)
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
                |import kotlinx.coroutines.flow.drop
                |import kotlinx.coroutines.flow.launchIn
                |import kotlinx.coroutines.flow.onEach
                |
                |internal fun ${plan.className}.attachFlowObserverGenerated() {
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
            |            Log.i("$tag", "change { previousState: ${'$'}$previousVar, currentState: ${'$'}$nextVar }")
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
            |            Log.i("$tag", "event { ${'$'}value }")
            |        }
            |        .launchIn(viewModelScope)
        """.trimMargin()
    }

    private fun previousVarName(propertyName: String): String =
        "previous${propertyName.replaceFirstChar { char -> char.uppercaseChar() }}"

    private fun nextVarName(propertyName: String): String =
        "next${propertyName.replaceFirstChar { char -> char.uppercaseChar() }}"

    private fun generateMaster(plan: FlowObserverPlan, originatingFiles: Array<KSFile>) {
        val vmImports = plan.viewModels.joinToString("\n") {
            "import ${it.qualifiedName}"
        }
        val extensionImports = plan.viewModels.joinToString("\n") {
            "import ${it.packageName}.attachFlowObserverGenerated"
        }
        val attachCalls = plan.viewModels.joinToString("\n") { vm ->
            """
                |        attach(ViewModelProvider(activity)[${vm.className}::class.java]) {
                |            it.attachFlowObserverGenerated()
                |        }
            """.trimMargin()
        }

        writeFile(
            packageName = Fqns.GENERATED_PACKAGE,
            fileName = "FlowObserverMaster",
            originatingFiles = originatingFiles,
            aggregating = true,
            content = """
                |package ${Fqns.GENERATED_PACKAGE}
                |
                |import androidx.activity.ComponentActivity
                |import androidx.lifecycle.ViewModel
                |import androidx.lifecycle.ViewModelProvider
                |import java.util.Collections
                |import java.util.WeakHashMap
                |$vmImports
                |$extensionImports
                |
                |object FlowObserverMaster {
                |
                |    private val attached = Collections.newSetFromMap(WeakHashMap<ViewModel, Boolean>())
                |
                |    fun attachAll(activity: ComponentActivity) {
                |$attachCalls
                |    }
                |
                |    private inline fun <T : ViewModel> attach(viewModel: T, block: (T) -> Unit) {
                |        if (!attached.add(viewModel)) return
                |        block(viewModel)
                |    }
                |}
            """.trimMargin(),
        )

        logger.warn("Generated FlowObserverMaster for ${plan.viewModels.size} view model(s)")
    }

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
