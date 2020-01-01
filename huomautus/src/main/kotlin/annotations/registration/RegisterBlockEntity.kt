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

package green.sailor.mc.huomautus.annotations.registration

/**
 * Marks a BlockEntity to be registered.
 *
 * The BE to be registered must be an abstract class; a concrete private implementation will be
 * generated and automatically registered.
 *
 * @param identifier: The identifier to register with e.g. mymod:some_block.
 * @param blocks: A list of identifiers for blocks registered for this BlockEntity.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class RegisterBlockEntity(
    val identifier: String,
    val blocks: Array<String>
)
