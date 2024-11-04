package io.github.ttypic.swiftklib.gradle.internal

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi
import io.github.ttypic.swiftklib.gradle.api.RemotePackageConfiguration
import io.github.ttypic.swiftklib.gradle.api.SwiftPackageConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal class SwiftPackageConfigurationImpl @Inject constructor(
    private val objects: ObjectFactory
) : SwiftPackageConfiguration {
    private val _dependencies =
        objects
            .listProperty(SwiftPackageDependency::class.java)
            .convention(emptyList())

    internal val dependencies get() = _dependencies

    @ExperimentalSwiftklibApi
    override fun local(name: String, path: java.io.File) {
        val currentDeps = _dependencies.get().toMutableList()
        currentDeps.add(SwiftPackageDependency.Local(listOf(name), path))
        _dependencies.set(currentDeps)
    }

    @ExperimentalSwiftklibApi
    override fun remote(
        name: List<String>,
        configuration: RemotePackageConfiguration.() -> Unit
    ) {
        val builder = RemotePackageConfigurationImpl(objects, name)
        builder.apply(configuration)

        val dependency = builder.build()
            ?: throw IllegalStateException("No version specification provided for remote package $name")

        val currentDeps = _dependencies.get().toMutableList()
        currentDeps.add(dependency)
        _dependencies.set(currentDeps)
    }

    @ExperimentalSwiftklibApi
    override fun remote(
        name: String,
        configuration: RemotePackageConfiguration.() -> Unit
    ) {
        val builder = RemotePackageConfigurationImpl(objects, listOf(name))
        builder.apply(configuration)

        val dependency = builder.build()
            ?: throw IllegalStateException("No version specification provided for remote package $name")

        val currentDeps = _dependencies.get().toMutableList()
        currentDeps.add(dependency)
        _dependencies.set(currentDeps)
    }
}
