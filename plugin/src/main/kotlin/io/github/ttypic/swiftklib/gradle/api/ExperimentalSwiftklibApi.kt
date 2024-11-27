package io.github.ttypic.swiftklib.gradle.api

@RequiresOptIn(
    message = "This API is experimental. It may be changed in the future without notice.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class ExperimentalSwiftklibApi {
}
