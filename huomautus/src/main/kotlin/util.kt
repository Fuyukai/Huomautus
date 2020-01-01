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

package green.sailor.mc.huomautus

import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

/**
 * Gets a class mirror from an annotation.
 */
fun <T> T.getClassMirror(field: (T) -> KClass<*>): TypeMirror {
    return try {
        field(this)
        error("Must never happen")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    } catch (e: MirroredTypesException) {
        e.typeMirrors.first()
    }
}

/**
 * Gets a list of class mirrors from a field annotation.
 */
fun <T> T.getClassMirrors(field: (T) -> List<KClass<*>>): List<TypeMirror> {
    try {
        field(this)
        error("Must never happen")
    } catch (e: MirroredTypesException) {
        return e.typeMirrors
    }
}
