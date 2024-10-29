package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import java.io.File
import javax.inject.Inject

@ExperimentalSwiftklibApi
class SwiftPackageDependencyHandler @Inject constructor(
    private val objects: ObjectFactory
) {
    private val _dependencies = objects.listProperty(SwiftPackageDependency::class.java).convention(emptyList())

    @get:Nested
    internal val dependencies: ListProperty<SwiftPackageDependency> = _dependencies

    @ExperimentalSwiftklibApi
    fun local(name: String, path: File) {
        val currentDeps = _dependencies.get().toMutableList()
        currentDeps.add(SwiftPackageDependency.Local(name, path))
        _dependencies.set(currentDeps)
    }

    @ExperimentalSwiftklibApi
    fun remote(name: String, block: RemotePackageBuilder.() -> Unit) {
        val builder = RemotePackageBuilder(objects, name)
        builder.apply(block)

        val dependency = builder.build()
            ?: throw IllegalStateException("No version specification provided for remote package $name")

        val currentDeps = _dependencies.get().toMutableList()
        currentDeps.add(dependency)
        _dependencies.set(currentDeps)
    }
}


