package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

abstract class SwiftKlibEntry @Inject constructor(
    val name: String,
    val objects: ObjectFactory,
) {
    val path: Property<File> = objects.property(File::class.java)
    val packageName: Property<String> = objects.property(String::class.java)
    val minIos: Property<Int> = objects.property(Int::class.java).convention(13)
    val minMacos: Property<Int> = objects.property(Int::class.java).convention(11)
    val minTvos: Property<Int> = objects.property(Int::class.java).convention(13)
    val minWatchos: Property<Int> = objects.property(Int::class.java).convention(8)

    internal val dependencies: ListProperty<SwiftPackageDependency> =
        objects.listProperty(SwiftPackageDependency::class.java)

    fun packageName(name: String) = packageName.set(name)

    @ExperimentalSwiftklibApi
    fun dependencies(action: Action<SwiftPackageDependencyHandler>) {
        val handler = SwiftPackageDependencyHandler(objects)
        action.execute(handler)
        dependencies.set(handler.dependencies)
    }
}

