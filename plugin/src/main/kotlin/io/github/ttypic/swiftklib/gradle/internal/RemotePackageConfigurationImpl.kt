package io.github.ttypic.swiftklib.gradle.internal

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import io.github.ttypic.swiftklib.gradle.api.RemotePackageConfiguration
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal class RemotePackageConfigurationImpl @Inject constructor(
    private val objects: ObjectFactory,
    private val name: String
) : RemotePackageConfiguration {
    private val urlProperty = objects.property(String::class.java)
    private val packageName = objects.property(String::class.java)
    private var dependency: SwiftPackageDependency.Remote? = null

    override fun github(owner: String, repo: String, packageName: String?) {
        require(owner.isNotBlank()) { "Owner cannot be blank" }
        require(repo.isNotBlank()) { "Repo cannot be blank" }
        urlProperty.set("https://github.com/$owner/$repo.git")
        this.packageName.set(packageName)
    }

    override fun url(url: String, packageName: String?) {
        require(url.isNotBlank()) { "URL cannot be blank" }
        urlProperty.set(url)
        this.packageName.set(packageName)
    }

    override fun exactVersion(version: String) {
        dependency = SwiftPackageDependency.Remote.ExactVersion(
            name = name,
            url = requireUrl(),
            version = version,
            packageName = packageName.orNull
        )
    }

    override fun versionRange(from: String, to: String, inclusive: Boolean) {
        dependency = SwiftPackageDependency.Remote.VersionRange(
            name = name,
            url = requireUrl(),
            from = from,
            to = to,
            inclusive = inclusive,
            packageName = packageName.orNull
        )
    }

    override fun branch(branchName: String) {
        dependency = SwiftPackageDependency.Remote.Branch(
            name = name,
            url = requireUrl(),
            branchName = branchName,
            packageName = packageName.orNull
        )
    }

    override fun fromVersion(version: String) {
        dependency = SwiftPackageDependency.Remote.FromVersion(
            name = name,
            url = requireUrl(),
            version = version,
            packageName = packageName.orNull
        )
    }

    internal fun build(): SwiftPackageDependency.Remote? = dependency

    private fun requireUrl(): String =
        urlProperty.orNull
            ?: throw IllegalStateException("URL must be set via github() or url() before specifying version")
}
