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
