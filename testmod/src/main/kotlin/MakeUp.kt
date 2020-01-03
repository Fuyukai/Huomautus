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

package green.sailor.mc.huomautus.test

import green.sailor.mc.testmod.generated.meta.TestModBlockEntities
import green.sailor.mc.testmod.generated.meta.TestModBlocks
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.util.Identifier

object MakeUp : ModInitializer {
    val itemGroup =
        FabricItemGroupBuilder.create(Identifier("testmod:group")).build()

    override fun onInitialize() {
        println("make up!")
        //TestModBlocks.register()
        //TestModBlockEntities.register()
    }
}
