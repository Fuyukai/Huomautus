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

import green.sailor.mc.huomautus.annotations.MixinImpl
import green.sailor.mc.huomautus.annotations.registration.RegisterBlock
import green.sailor.mc.huomautus.generators.BlocksGenerator
import green.sailor.mc.huomautus.generators.ProcessorState
import green.sailor.mc.huomautus.generators.generateJavaBridge
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@Suppress("unused")
@SupportedOptions(
    "kapt.kotlin.generated",
    "sailor.huomautus.package",
    "sailor.huomautus.prefix"
)
class Processor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(MixinImpl::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (roundEnv == null || roundEnv.processingOver()) return false

        val srcRoot = processingEnv.options["kapt.kotlin.generated"]
            ?: error("Cannot find source root")
        val packageName = processingEnv.options["sailor.huomautus.package"]
            ?: error("No package name specified!")
        val prefixName = processingEnv.options["sailor.huomautus.prefix"]
            ?: error("No prefix name specified!")

        val state = ProcessorState(srcRoot, packageName, prefixName)

        val mixins = roundEnv.getElementsAnnotatedWith(MixinImpl::class.java)
        for (elem in mixins) {
            if (elem.kind != ElementKind.CLASS) {
                error("Can only process class, not ${elem.kind}")
            }
            val genPackage = "$packageName.mixin"
            val bridge = generateJavaBridge(genPackage, elem as TypeElement)
            bridge.writeTo(Paths.get(srcRoot))
        }

        val blockGenAnnos = roundEnv.getElementsAnnotatedWith(RegisterBlock::class.java)
        val blockGen = BlocksGenerator(state)
        blockGen.generateBlockRegistration(blockGenAnnos)

        return true
    }
}
