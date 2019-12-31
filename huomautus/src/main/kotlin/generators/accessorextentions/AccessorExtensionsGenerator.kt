package green.sailor.mc.huomautus.generators.accessorextentions

import com.squareup.kotlinpoet.*
import green.sailor.mc.huomautus.generators.ProcessorState
import green.sailor.mc.huomautus.generators.accessor.javaToKotlinType
import green.sailor.mc.huomautus.getClassMirror
import org.spongepowered.asm.mixin.Mixin
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/** Small stub class for representing the field name. */
class AccessorField(
    val fieldName: String,
    val type: TypeName,
    val parent: TypeElement,
    val hasSetter: Boolean
)
val matcher = Regex("(?:get|is)(.*)")

/**
 * Generates accessor extensions.
 */
class AccessorExtensionsGenerator(val state: ProcessorState) {
    fun generateExtensions(accessor: TypeElement): List<PropertySpec> {
        // list of field namees
        val setters = accessor.enclosedElements
            .filter { it.simpleName.startsWith("set") }
            .map { it.simpleName.toString() }
            .toSet()

        val elements = accessor.enclosedElements
            .asSequence()
            .filterIsInstance<ExecutableElement>()  // should always be true, but smart casts
            .map { Pair(it.simpleName.toString(), it.returnType.asTypeName()) }
            .filter { it.first.startsWith("get") || it.first.startsWith("is") }
            .mapNotNull {
                val matched = matcher.find(it.first) ?: return@mapNotNull null
                Pair(matched.groupValues[1].decapitalize(), it.second)
            }
            .toList()

        val fields = elements.map {
            val setterName = "set${it.first.capitalize()}"
            val hasSetter = setterName in setters
            AccessorField(it.first, it.second, accessor, hasSetter)
        }

        return fields.map { generatePropertySpec(it) }
    }

    fun generatePropertySpec(field: AccessorField): PropertySpec {
        val kotlinType = field.type.javaToKotlinType()
        val builder = PropertySpec.builder(field.fieldName, kotlinType)
        // not pretty!
        // also, gives us a hard dep on mixin.
        val mixinAnnotation = field.parent.getAnnotation(Mixin::class.java)
        val accessorClass = mixinAnnotation.getClassMirror { it.value.first() }
        val accessorClassName = accessorClass.asTypeName()
        builder.addKdoc("Extension function accessing field " +
            "${field.fieldName} on $accessorClass")

        builder.receiver(accessorClassName)
        val getter =
            FunSpec.getterBuilder()
                .addStatement("return (this as %T).${field.fieldName}", field.parent)
                .build()
        // let the kotlin compiler do the hard work of matching up fields...
        builder.getter(getter)

        if (field.hasSetter) {
            builder.mutable(true)
            val setter = FunSpec.setterBuilder()
            setter.addParameter("value", kotlinType)
            setter.addStatement("(this as %T).${field.fieldName} = value", field.parent)
            builder.setter(setter.build())
        }
        return builder.build()
    }
}
