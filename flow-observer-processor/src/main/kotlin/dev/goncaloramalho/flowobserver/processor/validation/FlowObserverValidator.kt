package dev.goncaloramalho.flowobserver.processor.validation

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.goncaloramalho.flowobserver.FlowObserver
import dev.goncaloramalho.flowobserver.processor.model.FlowKind
import dev.goncaloramalho.flowobserver.processor.model.FlowObserverPlan
import dev.goncaloramalho.flowobserver.processor.model.LoggedFlow
import dev.goncaloramalho.flowobserver.processor.model.ViewModelLoggingPlan
import dev.goncaloramalho.flowobserver.processor.util.Fqns
import dev.goncaloramalho.flowobserver.processor.util.extendsViewModel
import dev.goncaloramalho.flowobserver.processor.util.flowKind
import dev.goncaloramalho.flowobserver.processor.util.fqn
import dev.goncaloramalho.flowobserver.processor.util.isPrivate
import dev.goncaloramalho.flowobserver.processor.util.qualifiedTypeName
import dev.goncaloramalho.flowobserver.processor.util.simpleTypeName

@OptIn(KspExperimental::class)
internal class FlowObserverValidator(
    private val logger: KSPLogger,
) {
    fun validate(resolver: Resolver): FlowObserverPlan {
        val annotatedProperties = resolver
            .getSymbolsWithAnnotation(Fqns.FLOW_OBSERVER)
            .filterIsInstance<KSPropertyDeclaration>()
            .toList()

        if (annotatedProperties.isEmpty()) {
            return FlowObserverPlan(emptyList(), emptyList())
        }

        val byViewModel = linkedMapOf<KSClassDeclaration, MutableList<LoggedFlow>>()
        val originatingFiles = linkedSetOf<com.google.devtools.ksp.symbol.KSFile>()

        for (property in annotatedProperties) {
            val parent = property.parentDeclaration as? KSClassDeclaration
            if (parent == null) {
                logger.error("@FlowObserver must be on a property inside a class", property)
                continue
            }

            if (!parent.extendsViewModel()) {
                logger.error(
                    "@FlowObserver property ${property.fqn()} must be declared in a ViewModel " +
                        "(extends androidx.lifecycle.ViewModel)",
                    property,
                )
                continue
            }

            if (property.isPrivate()) {
                logger.error(
                    "@FlowObserver property ${property.simpleName.asString()} must not be private; " +
                        "annotate the public StateFlow/SharedFlow instead",
                    property,
                )
                continue
            }

            val flowType = resolveFlowType(property) ?: continue
            val annotation = property.getAnnotationsByType(FlowObserver::class).firstOrNull()
                ?: continue

            val propertyName = property.simpleName.asString()
            val tag = annotation.tag.ifBlank { "${parent.simpleTypeName()}.$propertyName" }
            val flowKind = when (flowType.flowKind()) {
                Fqns.STATE_FLOW -> FlowKind.STATE_FLOW
                Fqns.SHARED_FLOW -> FlowKind.SHARED_FLOW
                else -> {
                    logger.error(
                        "@FlowObserver property $propertyName must be StateFlow or SharedFlow",
                        property,
                    )
                    continue
                }
            }

            property.containingFile?.let { originatingFiles += it }

            byViewModel.getOrPut(parent) { mutableListOf() } +=
                LoggedFlow(
                    propertyName = propertyName,
                    flowKind = flowKind,
                    tag = tag,
                    property = property,
                )
        }

        val viewModels = byViewModel.map { (klass, flows) ->
            ViewModelLoggingPlan(
                viewModel = klass,
                packageName = klass.packageName.asString(),
                className = klass.simpleTypeName(),
                qualifiedName = klass.qualifiedTypeName(),
                flows = flows,
            )
        }

        logger.warn(
            "FlowObserver: found ${viewModels.size} view model(s) with " +
                "${viewModels.sumOf { it.flows.size }} annotated flow(s)",
        )

        return FlowObserverPlan(viewModels, originatingFiles.toList())
    }

    private fun resolveFlowType(property: KSPropertyDeclaration): KSType? {
        val type = property.type.resolve()
        val flowFqn = type.flowKind()
        if (flowFqn != null) return type

        logger.error(
            "@FlowObserver property ${property.simpleName.asString()} must be StateFlow or SharedFlow, " +
                "found ${type.declaration.qualifiedName?.asString()}",
            property,
        )
        return null
    }
}
