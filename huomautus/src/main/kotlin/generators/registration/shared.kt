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
