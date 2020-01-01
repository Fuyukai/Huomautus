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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import green.sailor.mc.huomautus.annotations.registration.RegisterBlockEntity
import green.sailor.mc.huomautus.generators.ProcessorState
import java.nio.file.Paths
import java.util.function.Supplier
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

private val BLOCK_ENTITY_TYPE = ClassName(
    "net.minecraft.block.entity",
    "BlockEntityType"
)
private val BLOCK_ENTITY_BUILDER = ClassName(
    "net.minecraft.block.entity",
    "BlockEntityType", "Builder"
)
private val SUPPLIER = Supplier::class.asClassName()

/**
 * Represents a BlockEntity class and registration generator.
 */
class BlockEntitiesGenerator(val state: ProcessorState) {
    /**
     * Generates automatic registration for BlockEntity classes.
     */
    fun generateBERegistration(items: Set<Element>) {
        val file = FileSpec.builder(state.metaPackageName, state.beClass.simpleName)
        file.indent("    ")

        // $PrefixBlockEntities
        val mainClass = TypeSpec.objectBuilder(state.beClass)
        val mainClassRegister = FunSpec.builder("register")

        for (beClass in items) {
            // this shouldn't be true due to our AnnotationTarget, but whatever
            // it smart casts it
            if (beClass !is TypeElement) continue
            if (Modifier.ABSTRACT in beClass.modifiers) {
                error("Class $beClass is abstract and shouldn't be!")
            }
            val anno = beClass.getAnnotation(RegisterBlockEntity::class.java)

            // block entities are not singletons, so there's no concrete sub-impls.
            // we just create the stupid blockentitytype stuff.
            // calculate the field name
            val fieldName = anno.identifier.split(":")[1].toUpperCase()
            val beCN = beClass.asClassName()
            // BlockEntityType is a generic, so we need to make it BlockEntityType<BlockEntity>
            val beTypeParam = BLOCK_ENTITY_TYPE.parameterizedBy(beCN)

            // internal val BLOCK_ENTITY: BlockEntityType<BlockEntity>
            val beTypeField = PropertySpec.builder(fieldName, beTypeParam)
            beTypeField.addModifiers(KModifier.INTERNAL)

            // figure out the members for the block fields
            val members = anno.blocks.map {
                val identifier = it.split(":")[1]
                val blockField = identifier.toUpperCase()
                MemberName(state.blocksClass, blockField)
            }

            // this builds up the annoying calls
            // it's split over several lines to allow kotlinpoet to do imports propperly

            val supplierName = "__SUPPLIER_$fieldName"
            val supplierCall = PropertySpec.builder(
                supplierName, SUPPLIER.parameterizedBy(beCN)
            )
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.of("%T { %T() }", SUPPLIER, beCN))
                .build()
            mainClass.addProperty(supplierCall)

            // this will be ugly, but whatever...
            val memberList = members.joinToString(", ")
            val builderCall =
                CodeBlock.of(
                    "%T.create($supplierName, $memberList).build(null)",
                    BLOCK_ENTITY_BUILDER
                )
            beTypeField.initializer(builderCall)
            mainClass.addProperty(beTypeField.build())

            // add the register call
            val registerCall = CodeBlock.of(
                "%T.register(%T.BLOCK_ENTITY, %S, $fieldName)",
                REGISTRY, REGISTRY, anno.identifier
            )
            mainClassRegister.addCode(registerCall)
        }

        mainClassRegister.addKdoc("Registers all block entity types.")
        mainClassRegister.addModifiers(KModifier.PUBLIC)
        mainClass.addFunction(mainClassRegister.build())

        file.addType(mainClass.build())
        file.build().writeTo(Paths.get(state.srcRoot))
    }
}
