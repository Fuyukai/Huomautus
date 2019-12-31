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

import com.squareup.javapoet.*
import green.sailor.mc.huomautus.annotations.AutoAccessor
import green.sailor.mc.huomautus.generators.ProcessorState
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

private val MIXIN_NAME = ClassName.get(
    "org.spongepowered.asm.mixin",
    "Mixin"
)
private val ACCESSOR_NAME = ClassName.get(
    "org.spongepowered.asm.mixin.gen", "Accessor"
)
private val ACCESSOR_SPEC = AnnotationSpec.builder(ACCESSOR_NAME).build()

fun generateJavaInterface(iface: Element): TypeSpec {
    val mirror = try {
        iface.getAnnotation(AutoAccessor::class.java).klass
        error("shouldn't happen")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }
    // this loads the real class, e.g. @AutoAccessor(MinecraftClient::class) loads
    // MinecraftClient
    // interface <iface> {
    val builder = TypeSpec.interfaceBuilder(iface.simpleName.toString())
    // add the mixin annotation
    builder.addAnnotation(AnnotationSpec.builder(MIXIN_NAME).apply {
        addMember("value", "$mirror.class")
    }.build())

    // figure out what fields we need to make accessors for
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

        val getterBuilder = MethodSpec.methodBuilder(getterName)
        getterBuilder.returns(TypeName.get(field.asType()))
        getterBuilder.addAnnotation(ACCESSOR_SPEC)
        getterBuilder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
        val isFinal = Modifier.FINAL in field.modifiers
        val jdoc = if (isFinal) {
            """
Automatically generated getter accessor for field ${field.simpleName}
This field is final; cannot create setter.

            """.trimMargin()
        } else {
            "Automatically generated getter accessor for field ${field.simpleName}\n"
        }

        getterBuilder.addJavadoc(jdoc)
        val getter = getterBuilder.build()
        builder.addMethod(getter)

        // can't set final fields.
        if (isFinal) continue

        val setterName = "set$fixedName"
        val setterBuilder = MethodSpec.methodBuilder(setterName)
        setterBuilder.addAnnotation(ACCESSOR_SPEC)
        setterBuilder.addParameter(TypeName.get(field.asType()), "value")
        setterBuilder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
        getterBuilder.addJavadoc("Automatically generated setter accessor for field " +
            "${field.simpleName}")
        val setter = setterBuilder.build()
        builder.addMethod(setter)
    }
    builder.addJavadoc("Automatically generated accessor mixin for $mirror")
    builder.addModifiers(Modifier.PUBLIC)

    return builder.build()
}

/**
 * Writes the accessor interface to a file.
 */
fun toJavaFile(state: ProcessorState, spec: TypeSpec): JavaFile {
    val builder = JavaFile.builder(
        state.genPackageName + ".mixin",
        spec
    )
    builder.addFileComment("Automatically generated accessor mixin. Do not edit!")
    builder.indent("    ")
    val file = builder.build()
    return file
}
