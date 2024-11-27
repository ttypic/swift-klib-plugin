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
    internal val dependencies =
        objects
            .listProperty(SwiftPackageDependency::class.java)
            .convention(emptyList())


    @ExperimentalSwiftklibApi
    override fun local(name: String, path: java.io.File) {
        dependencies.add(SwiftPackageDependency.Local(listOf(name), path))
    }

    @ExperimentalSwiftklibApi
    override fun remote(
        name: String,
        configuration: RemotePackageConfiguration.() -> Unit
    ) {
        remote(listOf(name), configuration)
    }

    @ExperimentalSwiftklibApi
    override fun remote(
        names: List<String>,
        configuration: RemotePackageConfiguration.() -> Unit
    ) {
        val builder = RemotePackageConfigurationImpl(objects, names)
        builder.apply(configuration)

        val dependency = builder.build()
            ?: throw IllegalStateException("No version specification provided for remote package ${names.joinToString(", ")}")

        dependencies.add(dependency)
    }

}
