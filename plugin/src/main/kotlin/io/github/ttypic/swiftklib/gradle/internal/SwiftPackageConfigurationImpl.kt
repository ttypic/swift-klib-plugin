package io.github.ttypic.swiftklib.gradle.internal

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi
import io.github.ttypic.swiftklib.gradle.api.RemotePackageConfiguration
import io.github.ttypic.swiftklib.gradle.api.SwiftPackageConfiguration
import org.gradle.api.model.ObjectFactory
import java.io.File
import java.net.URI
import java.net.URL
import javax.inject.Inject

internal class SwiftPackageConfigurationImpl @Inject constructor(
    private val objects: ObjectFactory
) : SwiftPackageConfiguration {
    internal val dependencies =
        objects
            .listProperty(SwiftPackageDependency::class.java)
            .convention(emptyList())

    @ExperimentalSwiftklibApi
    override fun local(name: String, path: File) {
        val currentDeps = dependencies.get().toMutableList()
        currentDeps.add(SwiftPackageDependency.Local(listOf(name), path))
        dependencies.set(currentDeps)
    }

    override fun localBinary(name: String, path: File) {
        val currentDeps = dependencies.get().toMutableList()
        currentDeps.add(SwiftPackageDependency.LocalBinary(listOf(name), path))
        dependencies.set(currentDeps)
    }

    override fun remoteBinary(name: String, url: URI, checksum: String) {
        val currentDeps = dependencies.get().toMutableList()
        currentDeps.add(SwiftPackageDependency.RemoteBinary(listOf(name), url, checksum))
        dependencies.set(currentDeps)
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

        val currentDeps = dependencies.get().toMutableList()
        currentDeps.add(dependency)
        dependencies.set(currentDeps)
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

        val currentDeps = dependencies.get().toMutableList()
        currentDeps.add(dependency)
        dependencies.set(currentDeps)
    }
}
