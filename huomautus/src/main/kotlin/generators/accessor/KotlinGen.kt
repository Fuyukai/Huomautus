/*
 * This file is part of huomautus.
 *
 * huomautus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * huomautus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with huomautus.  If not, see <https://www.gnu.org/licenses/>.
 */

package green.sailor.mc.huomautus.generators.accessor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import green.sailor.mc.huomautus.annotations.AutoAccessor
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

// https://github.com/square/kotlinpoet/issues/236
fun TypeName.javaToKotlinType(): TypeName {
    return when (this) {
        is ParameterizedTypeName -> {
            (rawType.javaToKotlinType() as ClassName)
                .parameterizedBy(*(typeArguments.map { it.javaToKotlinType() }.toTypedArray()))
        }
        is WildcardTypeName -> {
            outTypes[0].javaToKotlinType()
        }
        else -> {
            val className =
                JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(
                    FqName(toString())
                )?.asSingleFqName()?.asString()
            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}

/**
 * Generates a list of Kotlin extension functions given a mixin accessor.
 */
fun generateKotlinExtensionFunctions(className: ClassName, iface: Element): List<PropertySpec> {
    // todo: This is all copy/pasted, could feasibly add something common together.
    val mirror = try {
        iface.getAnnotation(AutoAccessor::class.java).klass
        error("shouldn't happen")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }

    val realClass = (mirror as DeclaredType).asElement() as TypeElement
    val exists = realClass.enclosedElements
        .filter {
            it.kind == ElementKind.METHOD && it.simpleName.matches("(get|set|is).*".toRegex())
        }
        .map { (it as ExecutableElement).simpleName.toString() }
        .toSet()

    val fields = realClass.enclosedElements
        .filter { it.kind == ElementKind.FIELD }
        .map { it as VariableElement }
        .filter { Modifier.PUBLIC !in it.modifiers && Modifier.STATIC !in it.modifiers }

    // real logic
    val fns = mutableListOf<PropertySpec>()
    for (field in fields) {
        if (field.simpleName.toString() in exists) continue
        val fixedName = field.simpleName.toString().capitalize()
        val varType = field.asType()
        val getterName = if (varType.toString() == "boolean") {
            "is$fixedName"
        } else {
            "get$fixedName"
        }
        if (getterName in exists) continue

        // actual code
        // translate typenames to kotlin typenames

        val propBuilder = PropertySpec.builder(
            field.simpleName.toString(),
            varType.asTypeName().javaToKotlinType()
        )
        propBuilder.receiver(mirror.asTypeName())
        propBuilder.getter(FunSpec.getterBuilder().apply {
            addStatement("return (this as %T).$getterName()", className)
        }.build())
        fns.add(propBuilder.build())
    }

    return fns
}
