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

package green.sailor.mc.huomautus.generators.registration

import com.squareup.kotlinpoet.*
import green.sailor.mc.huomautus.annotations.registration.RegisterBlock
import green.sailor.mc.huomautus.generators.ProcessorState
import java.nio.file.Paths
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Represents the block class generator.
 */
class BlocksGenerator(val state: ProcessorState) {

    /**
     * Generates a register function for a block.
     */
    fun generateRegisterSpec(name: String): FunSpec {
        val method = FunSpec.builder("register").apply {
            addKdoc("Registers this block in the Block registry.")
            addStatement("val identifier = %T(\"$name\")", IDENTIFIER)
            addStatement(
                "%T.register(%T.BLOCK, identifier, this)",
                REGISTRY,
                REGISTRY
            )
            returns(Unit::class.java)
        }.build()
        return method
    }

    /**
     * Generates a BlockItem registry specification.
     */
    fun generateBIRegisterSpec(fieldName: String, name: String, group: String): CodeBlock {
        return CodeBlock.of(
            "%T.register(%T.ITEM, %T(\"$name\"), %T($fieldName, %T().group($group)))",
            REGISTRY, REGISTRY,
            IDENTIFIER,
            BLOCK_ITEM, ITEM_SETTINGS
        )
    }

    fun generateBlockRegistration(items: Set<Element>) {
        val file = FileSpec.builder(
            state.metaPackageName, state.blocksClass.simpleName
        )
        file.indent("   ")
        val mainClass = TypeSpec.objectBuilder(state.blocksClass)
        val mainClassRegister = FunSpec.builder("register")
        // map of temp item group field getters
        val itemGroupTempFields = mutableMapOf<String, PropertySpec>()

        for (annotated in items) {
            // only process classes...
            if (annotated !is TypeElement) continue
            if (!annotated.modifiers.contains(Modifier.ABSTRACT)) {
                error("Class $annotated is not abstract!")
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
            mainClassRegister.addStatement("($fieldName as $name).register()")

            // itemblock registration
            if (anno.autoItemBlock) {
                val itemGroupId = anno.inItemGroup
                // either load or build a new item group field
                // this will make a lazy-delegated field that searches for the item group
                // in the list of item groups by identifier
                // not pretty. but it'll do.
                val itemGroupField = if (itemGroupId !in itemGroupTempFields) {
                    val igField = generateItemGroupCacher(itemGroupId)
                    mainClass.addProperty(igField)
                    itemGroupTempFields[itemGroupId] = igField
                    igField.name
                } else {
                    itemGroupTempFields[itemGroupId]!!.name
                }

                val ibRegStmnt = generateBIRegisterSpec(
                    fieldName, anno.identifier, itemGroupField
                )
                mainClassRegister.addCode(ibRegStmnt)
            }

            // finish up by adding the actual type object
            file.addType(implClass)
        }

        mainClassRegister.addKdoc("Registers all blocks.")
        mainClassRegister.addModifiers(KModifier.PUBLIC)
        mainClass.addFunction(mainClassRegister.build())

        file.addType(mainClass.build())
        file.build().writeTo(Paths.get(state.srcRoot))
    }
}
