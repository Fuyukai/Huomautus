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

package green.sailor.mc.huomautus.generators.accessor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import green.sailor.mc.huomautus.generators.ProcessorState
import java.nio.file.Paths
import javax.lang.model.element.Element

/**
 * Generates accessor mixins.
 */
class AccessorGenerator(val processorState: ProcessorState) {
    fun generateAccessors(fakeInterfaces: Set<Element>) {
        // two-pass generation
        // pass one is to generate the actual accessor
        // pass two is to generate kotlin ext properties
        val properties = fakeInterfaces.flatMap {
            val javaClass = generateJavaInterface(it)
            val javaFile = toJavaFile(processorState, javaClass)
            javaFile.writeTo(Paths.get(processorState.srcRoot))
            val className = ClassName(javaFile.packageName, javaClass.name)
            generateKotlinExtensionFunctions(className, it)
        }
        val fileSpec = FileSpec.builder(
            processorState.genPackageName,
            "extensions"
        )
        properties.forEach { fileSpec.addProperty(it) }
        fileSpec.build().writeTo(Paths.get(processorState.srcRoot))
    }
}
