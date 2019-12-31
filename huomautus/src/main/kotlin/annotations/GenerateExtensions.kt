package green.sailor.mc.huomautus.annotations

/**
 * Marks an accessor for Kotlin extension function generation.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class GenerateExtensions {
}
