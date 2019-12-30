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

/**
 * Represents the processor state.
 */
data class ProcessorState(
    val srcRoot: String,
    val genPackageName: String,
    val genPrefix: String
) {
    val blocksClass = ClassName(
        "$genPackageName.meta",
        "${genPrefix}Blocks"
    )

    val itemsClass = ClassName(
        "$genPackageName.meta",
        "${genPrefix}Items"
    )
}
