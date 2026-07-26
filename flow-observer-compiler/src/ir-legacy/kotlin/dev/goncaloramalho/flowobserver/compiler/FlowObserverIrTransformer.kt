package dev.goncaloramalho.flowobserver.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * IR transformer for Kotlin hosts that still use the pre-[IrParameterKind] call API
 * (Kotlin 2.0.x and 2.1.0/2.1.10).
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
class FlowObserverIrTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {

    private val observeFlowAnnotationFqName =
        FqName("dev.goncaloramalho.flowobserver.ObserveFlow")

    private val subscriptionLoggingClassId =
        ClassId(FqName("dev.goncaloramalho.flowobserver"), Name.identifier("SubscriptionLogging"))

    private val addObservableCallableId =
        CallableId(
            FqName("dev.goncaloramalho.flowobserver"),
            Name.identifier("addObservable"),
        )

    private val mutableStateFlowClassId =
        ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("MutableStateFlow"))

    private val mutableSharedFlowClassId =
        ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("MutableSharedFlow"))

    private val mutableStateFlowFqName = mutableStateFlowClassId.asSingleFqName()
    private val mutableSharedFlowFqName = mutableSharedFlowClassId.asSingleFqName()

    private val viewModelClassId =
        ClassId(FqName("androidx.lifecycle"), Name.identifier("ViewModel"))

    override fun visitProperty(declaration: IrProperty): IrStatement {
        if (!declaration.hasAnnotation(observeFlowAnnotationFqName)) {
            return super.visitProperty(declaration)
        }

        val parentClass = declaration.parentClassOrNull
            ?: return super.visitProperty(declaration)

        if (!parentClass.extendsViewModel()) {
            return super.visitProperty(declaration)
        }

        val field = declaration.backingField
        val initializerExpression = field?.initializer?.expression
        if (field == null || initializerExpression == null) {
            return super.visitProperty(declaration)
        }

        if (initializerExpression.isAlreadyAddObservable()) {
            return super.visitProperty(declaration)
        }

        val flowKind = flowKindOf(initializerExpression.type)
            ?: flowKindOf(declaration.getter?.returnType)
            ?: return super.visitProperty(declaration)

        val addObservableSymbol = resolveAddObservable(flowKind)
            ?: return super.visitProperty(declaration)

        val (tag, subscriptionLoggingName) = declaration.resolveObserveFlowArgs(parentClass)
        val typeArgument = (initializerExpression.type as? IrSimpleType)
            ?.arguments
            ?.filterIsInstance<IrTypeProjection>()
            ?.firstOrNull()
            ?.type
            ?: pluginContext.irBuiltIns.anyNType

        val builder = DeclarationIrBuilder(
            pluginContext,
            field.symbol,
            field.startOffset,
            field.endOffset,
        )

        val wrapped: IrExpression = builder.irCall(addObservableSymbol).apply {
            type = initializerExpression.type
            extensionReceiver = initializerExpression
            if (typeArgumentsCount > 0) {
                putTypeArgument(0, typeArgument)
            }
            putValueArgument(0, builder.irString(tag))
            putValueArgument(1, builder.irSubscriptionLogging(subscriptionLoggingName))
        }

        field.initializer = pluginContext.irFactory.createExpressionBody(
            startOffset = field.startOffset,
            endOffset = field.endOffset,
            expression = wrapped,
        )

        return super.visitProperty(declaration)
    }

    private fun IrBuilderWithScope.irSubscriptionLogging(entryName: String): IrExpression {
        val enumClass = pluginContext.referenceClass(subscriptionLoggingClassId)
            ?: error("SubscriptionLogging is not on the compilation classpath")
        val entry = enumClass.owner.declarations
            .filterIsInstance<IrEnumEntry>()
            .firstOrNull { it.name.asString() == entryName }
            ?: error("Unknown SubscriptionLogging.$entryName")
        return IrGetEnumValueImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = enumClass.defaultType,
            symbol = entry.symbol,
        )
    }

    private fun IrClass.extendsViewModel(): Boolean {
        val viewModel = pluginContext.referenceClass(viewModelClassId) ?: return true
        return isSubclassOf(viewModel.owner)
    }

    private fun IrExpression.isAlreadyAddObservable(): Boolean {
        val call = this as? IrCall ?: return false
        val owner = call.symbol.owner
        if (owner.name.asString() != "addObservable") return false
        return owner.fqNameWhenAvailable?.parent()?.asString() == "dev.goncaloramalho.flowobserver"
    }

    private fun IrProperty.resolveObserveFlowArgs(parentClass: IrClass): Pair<String, String> {
        val annotation = annotations.firstOrNull { it.isObserveFlowAnnotation() }
            ?: return defaultTag(parentClass) to SubscriptionLoggingNames.DEFAULT

        var tag: String? = null
        var subscriptionLogging = SubscriptionLoggingNames.DEFAULT

        val constructor = annotation.symbol.owner
        for ((index, parameter) in constructor.valueParameters.withIndex()) {
            val argument = annotation.getValueArgument(index) ?: continue
            when (parameter.name.asString()) {
                "tag" -> tag = (argument as? IrConst<*>)?.value as? String
                "subscriptionLogging" -> {
                    val enumGet = argument as? IrGetEnumValue
                    if (enumGet != null) {
                        subscriptionLogging = enumGet.symbol.owner.name.asString()
                    }
                }
            }
        }

        val resolvedTag = if (tag.isNullOrBlank()) defaultTag(parentClass) else tag
        return resolvedTag to subscriptionLogging
    }

    private fun IrConstructorCall.isObserveFlowAnnotation(): Boolean =
        type.classFqName == observeFlowAnnotationFqName

    private fun IrProperty.defaultTag(parentClass: IrClass): String =
        "${parentClass.name.asString()}.${name.asString()}"

    private fun resolveAddObservable(flowKind: FlowKind) =
        pluginContext.referenceFunctions(addObservableCallableId).firstOrNull { symbol ->
            val extensionFqName = symbol.owner.extensionReceiverParameter?.type?.classFqName
            when (flowKind) {
                FlowKind.STATE -> extensionFqName == mutableStateFlowFqName
                FlowKind.SHARED -> extensionFqName == mutableSharedFlowFqName
            }
        }

    private fun flowKindOf(type: IrType?): FlowKind? {
        if (type == null) return null
        val mutableState = pluginContext.referenceClass(mutableStateFlowClassId) ?: return null
        val mutableShared = pluginContext.referenceClass(mutableSharedFlowClassId) ?: return null
        return when {
            type.isSubtypeOfClass(mutableState) -> FlowKind.STATE
            type.isSubtypeOfClass(mutableShared) -> FlowKind.SHARED
            else -> null
        }
    }

    private enum class FlowKind { STATE, SHARED }

    private object SubscriptionLoggingNames {
        const val DEFAULT = "Default"
    }
}
