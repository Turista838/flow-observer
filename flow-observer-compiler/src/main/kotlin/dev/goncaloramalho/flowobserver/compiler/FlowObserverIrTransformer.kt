package dev.goncaloramalho.flowobserver.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classFqName
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

@OptIn(UnsafeDuringIrConstructionAPI::class)
class FlowObserverIrTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {

    private val observeFlowAnnotationFqName =
        FqName("dev.goncaloramalho.flowobserver.ObserveFlow")

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

        val tag = declaration.resolveTag(parentClass)
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

        val callee = addObservableSymbol.owner
        val wrapped: IrExpression = builder.irCall(addObservableSymbol).apply {
            type = initializerExpression.type
            if (typeArguments.isNotEmpty()) {
                typeArguments[0] = typeArgument
            }
            for ((index, parameter) in callee.parameters.withIndex()) {
                arguments[index] = when (parameter.kind) {
                    IrParameterKind.ExtensionReceiver -> initializerExpression
                    IrParameterKind.Regular -> builder.irString(tag)
                    else -> null
                }
            }
        }

        field.initializer = pluginContext.irFactory.createExpressionBody(
            startOffset = field.startOffset,
            endOffset = field.endOffset,
            expression = wrapped,
        )

        return super.visitProperty(declaration)
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

    private fun IrProperty.resolveTag(parentClass: IrClass): String {
        val annotation = annotations.firstOrNull { it.isObserveFlowAnnotation() }
            ?: return defaultTag(parentClass)

        val tagArg = annotation.arguments.firstOrNull() as? IrConst
        val tag = tagArg?.value as? String
        return if (tag.isNullOrBlank()) defaultTag(parentClass) else tag
    }

    private fun IrConstructorCall.isObserveFlowAnnotation(): Boolean =
        type.classFqName == observeFlowAnnotationFqName

    private fun IrProperty.defaultTag(parentClass: IrClass): String =
        "${parentClass.name.asString()}.${name.asString()}"

    private fun resolveAddObservable(flowKind: FlowKind) =
        pluginContext.referenceFunctions(addObservableCallableId).firstOrNull { symbol ->
            val extensionFqName = symbol.owner.parameters
                .firstOrNull { it.kind == IrParameterKind.ExtensionReceiver }
                ?.type
                ?.classFqName
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
}
