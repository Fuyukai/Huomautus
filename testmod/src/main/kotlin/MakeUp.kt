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

import green.sailor.mc.testmod.generated.TestModBlocks
import green.sailor.mc.testmod.generated.fpsCounter
import kotlin.random.Random
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import net.minecraft.client.MinecraftClient

object MakeUp : ModInitializer {
    override fun onInitialize() {
        TestModBlocks.register()
        ServerTickCallback.EVENT.register(ServerTickCallback {
            if (Random.nextInt(20) == 1) {
                println("FPS is: ${MinecraftClient.getInstance().fpsCounter}")
            }
        })
    }
}
