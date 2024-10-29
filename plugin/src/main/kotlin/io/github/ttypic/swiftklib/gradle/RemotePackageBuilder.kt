package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

@ExperimentalSwiftklibApi
class RemotePackageBuilder @Inject constructor(
    private val objects: ObjectFactory,
    private val name: String
) {
    private val urlProperty: Property<String> = objects.property(String::class.java)
    private var dependency: SwiftPackageDependency.Remote? = null
        private set

    @ExperimentalSwiftklibApi
    fun github(owner: String, repo: String) {
        require(owner.isNotBlank()) { "Owner cannot be blank" }
        require(repo.isNotBlank()) { "Repo cannot be blank" }

        urlProperty.set("https://github.com/$owner/$repo.git")
    }

    @ExperimentalSwiftklibApi
    fun url(url: String) {
        require(url.isNotBlank()) { "URL cannot be blank" }
        urlProperty.set(url)
    }

    @ExperimentalSwiftklibApi
    fun exactVersion(version: String) {
        dependency = SwiftPackageDependency.Remote.ExactVersion(
            name = name,
            url = requireUrl(),
            version = version
        )
    }

    @ExperimentalSwiftklibApi
    fun versionRange(
        from: String,
        to: String,
        inclusive: Boolean = true
    ) {
        dependency = SwiftPackageDependency.Remote.VersionRange(
            name = name,
            url = requireUrl(),
            from = from,
            to = to,
            inclusive = inclusive
        )
    }

    @ExperimentalSwiftklibApi
    fun branch(branchName: String) {
        dependency = SwiftPackageDependency.Remote.Branch(
            name = name,
            url = requireUrl(),
            branchName = branchName
        )
    }

    @ExperimentalSwiftklibApi
    fun fromVersion(version: String) {
        dependency = SwiftPackageDependency.Remote.FromVersion(
            name = name,
            url = requireUrl(),
            version = version
        )
    }

    @ExperimentalSwiftklibApi
    internal fun build(): SwiftPackageDependency.Remote? = dependency

    private fun requireUrl(): String =
        urlProperty.orNull
            ?: throw IllegalStateException("URL must be set via github() or url() before specifying version")
}
