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

@file:JvmName("JavaBridgeKt")
package green.sailor.mc.huomautus.generators

import com.squareup.javapoet.*
import green.sailor.mc.huomautus.annotations.MixinImpl
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

private val MIXIN_NAME = ClassName.get(
    "org.spongepowered.asm.mixin",
    "Mixin"
)
private val UNIT = ClassName.get("kotlin", "Unit")

/**
 * Adds a mixin method to a builder.
 *
 * This generates a simple bridge function that just calls the original element.
 */
fun addMixinMethod(builder: TypeSpec.Builder, meth: ExecutableElement) {
    val parent = meth.enclosingElement
    val parentType = TypeName.get(parent.asType())
    val methodBuilder =
        MethodSpec.methodBuilder("bridge__${meth.simpleName}")

    // copy all annotations from the kotlin function
    for (annotation in meth.annotationMirrors) {
        val annoSpec = AnnotationSpec.get(annotation)
        methodBuilder.addAnnotation(annoSpec)
    }

    // also copy all parameters
    val params = meth.parameters
    for (param in params) {
        methodBuilder.addParameter(ParameterSpec.get(param))
    }
    methodBuilder.returns(TypeName.get(meth.returnType))

    val paramString = meth.parameters.joinToString(", ") { it.simpleName }
    val isVoid =
        meth.returnType.toString() == "void" || meth.returnType.toString() == "kotlin.Unit"
    val code = if (isVoid) {
        CodeBlock.builder().add(
            "\$L.\$L.\$L($paramString)",
            parentType.toString(), "INSTANCE", meth.simpleName
        )
    } else {
        CodeBlock.builder().add(
            "return \$L.\$L.\$L($paramString)",
            parentType.toString(), "INSTANCE", meth.simpleName
        )
    }.build()

    methodBuilder.addStatement(code)

    builder.addMethod(methodBuilder.build())
}

fun generateJavaBridge(genPackage: String, elem: TypeElement): JavaFile {
    val name = "GeneratedBridge${elem.simpleName}"
    val anno = elem.getAnnotation(MixinImpl::class.java)
    // lol
    val annoClassName = try {
        anno.value
        // will never happen
        error("Must never happen")
    } catch (e: MirroredTypeException) {
        e.typeMirror.toString()
    }

    val spec = TypeSpec.classBuilder(name)
    spec.apply {
        val anno = AnnotationSpec.builder(MIXIN_NAME).apply {
            addMember("value", "$annoClassName.class")
        }.build()
        addAnnotation(anno)
    }

    val children = elem.enclosedElements
    loop@for (child in children) { when (child) {
        // method
        is ExecutableElement -> {
            if (child.simpleName.toString() == "<init>") continue@loop
            addMixinMethod(spec, child)
        }
    } }

    val type = spec.build()

    val builder = JavaFile.builder(genPackage, type)
    builder.addFileComment("Automatically generated Mixin bridge.")
    return builder.build()
}
