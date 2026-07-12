package dev.goncaloramalho.flowobserver.processor.util

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

internal fun KSDeclaration.fqn(): String =
    qualifiedName?.asString() ?: simpleName.asString()

@OptIn(KspExperimental::class)
internal fun KSClassDeclaration.extendsViewModel(): Boolean =
    superTypes.any { superType ->
        val declaration = superType.resolve().declaration
        val qn = declaration.qualifiedName?.asString() ?: return@any false
        qn == Fqns.VIEW_MODEL ||
            qn == Fqns.ANDROID_VIEW_MODEL ||
            (declaration is KSClassDeclaration && declaration.extendsViewModel())
    }

internal fun KSPropertyDeclaration.isPrivate(): Boolean =
    modifiers.contains(Modifier.PRIVATE)

internal fun KSType.flowKind(): String? {
    val qn = declaration.qualifiedName?.asString() ?: return null
    if (qn == Fqns.STATE_FLOW) return Fqns.STATE_FLOW
    if (qn == Fqns.SHARED_FLOW) return Fqns.SHARED_FLOW
    return null
}

internal fun KSClassDeclaration.simpleTypeName(): String = simpleName.asString()

internal fun KSClassDeclaration.qualifiedTypeName(): String =
    "${packageName.asString()}.${simpleTypeName()}"
