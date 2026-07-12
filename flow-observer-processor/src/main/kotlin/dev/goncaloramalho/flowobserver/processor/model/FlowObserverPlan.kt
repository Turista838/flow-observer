package dev.goncaloramalho.flowobserver.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

internal data class FlowObserverPlan(
    val viewModels: List<ViewModelLoggingPlan>,
    val originatingFiles: List<KSFile>,
)

internal data class ViewModelLoggingPlan(
    val viewModel: KSClassDeclaration,
    val packageName: String,
    val className: String,
    val qualifiedName: String,
    val flows: List<LoggedFlow>,
)

internal data class LoggedFlow(
    val propertyName: String,
    val flowKind: FlowKind,
    val tag: String,
    val logInitial: Boolean,
    val property: KSPropertyDeclaration,
)

internal enum class FlowKind {
    STATE_FLOW,
    SHARED_FLOW,
}
