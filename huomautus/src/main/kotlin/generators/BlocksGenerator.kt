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

package green.sailor.mc.huomautus.generators

import com.squareup.kotlinpoet.*
import green.sailor.mc.huomautus.annotations.registration.RegisterBlock
import java.nio.file.Paths
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Represents the block class generator.
 */
class BlocksGenerator(val state: ProcessorState) {
    companion object {
        val identifierClassname = ClassName("net.minecraft.util", "Identifier")
        val registryClassname = ClassName("net.minecraft.util.registry", "Registry")
    }

    /**
     * Generates a register function for a block.
     */
    fun generateRegisterSpec(name: String): FunSpec {
        val method = FunSpec.builder("register").apply {
            addKdoc("Registers this block in the Block registry.")
            addStatement("val identifier = %T(\"$name\")", identifierClassname)
            addStatement(
                "%T.register(%T.BLOCK, identifier, this)",
                registryClassname, registryClassname
            )
            returns(Unit::class.java)
        }.build()
        return method
    }

    fun generateBlockRegistration(items: Set<Element>) {
        val file = FileSpec.builder(
            state.genPackageName, state.blocksClass.simpleName
        )
        file.indent("   ")
        val mainClass = TypeSpec.objectBuilder(state.blocksClass)
        val mainClassRegister = FunSpec.builder("register")
        for (annotated in items) {
            // only process classes...
            if (annotated !is TypeElement) continue
            if (!annotated.modifiers.contains(Modifier.ABSTRACT)) {
                error("$annotated is not abstract!")
            }
            val anno = annotated.getAnnotation(RegisterBlock::class.java)

            // private class <name>RegisteredImpl
            val name = annotated.simpleName.toString() + "RegisteredImpl"
            val implClassB = TypeSpec.classBuilder(name)
            implClassB.addModifiers(KModifier.PRIVATE)
            implClassB.superclass(annotated.asType().asTypeName())
            implClassB.addFunction(generateRegisterSpec(anno.identifier))

            // TODO: Stuff like BlockEntityProvider

            val implClass = implClassB.build()

            // add the fields and the register call to the main class
            val fieldName = anno.identifier.split(":")[1].toUpperCase()

            // @JvmStatic val NAME: SomeBlock = SomeBlockRegisteredImpl()
            val field = PropertySpec.builder(fieldName, annotated.asClassName())
            field.addAnnotation(JvmStatic::class.java)
            field.initializer("$name()")
            field.addModifiers(KModifier.PUBLIC)
            mainClass.addProperty(field.build())
            mainClassRegister.addStatement("(${fieldName} as $name).register()")

            file.addType(implClass)
        }

        mainClassRegister.addKdoc("Registers all blocks.")
        mainClassRegister.addModifiers(KModifier.PUBLIC)
        mainClass.addFunction(mainClassRegister.build())

        file.addType(mainClass.build())
        file.build().writeTo(Paths.get(state.srcRoot))
    }
}
