package green.sailor.mc.huomautus.generators.registration

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

private val ITEM_GROUP = ClassName("net.minecraft.item", "ItemGroup")
val IDENTIFIER = ClassName("net.minecraft.util", "Identifier")
val REGISTRY = ClassName("net.minecraft.util.registry", "Registry")
val BLOCK_ITEM = ClassName("net.minecraft.item", "BlockItem")
val ITEM_SETTINGS = ClassName("net.minecraft.item", "Item", "Settings")

/**
 * Generates an ItemGroup field finder from an identifier.
 */
fun generateItemGroupCacher(identifier: String): PropertySpec {
    val registeredName = identifier.replace(":", ".")
    val splitName = identifier.split(":")[1]
    val fieldName = "__CACHED_ITEM_GROUP_$splitName"
    val prop = PropertySpec.builder(fieldName, ITEM_GROUP)

    val filterer = CodeBlock.of(
        "lazy { %T.GROUPS.first { it.name == %S } }",
        ITEM_GROUP, registeredName
    )

    prop.addModifiers(KModifier.PRIVATE)
    prop.delegate(filterer)
    prop.addKdoc("Cached field loader for $identifier")
    return prop.build()
}
