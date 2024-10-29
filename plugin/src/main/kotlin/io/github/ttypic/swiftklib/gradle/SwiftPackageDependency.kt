package io.github.ttypic.swiftklib.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import java.io.File
import java.io.Serializable

internal sealed interface SwiftPackageDependency : Serializable {
    @get:Input
    val name: String

    data class Local(
        @Input override val name: String,
        @InputDirectory val path: File
    ) : SwiftPackageDependency {
        init {
            require(name.isNotBlank()) { "Package name cannot be blank" }
            require(path.exists()) { "Package path must exist: $path" }
        }
    }

    sealed interface Remote : SwiftPackageDependency {
        @get:Input
        val url: String

        data class ExactVersion(
            @Input override val name: String,
            @Input override val url: String,
            @Input val version: String
        ) : Remote {
            init {
                require(name.isNotBlank()) { "Package name cannot be blank" }
                require(url.isNotBlank()) { "URL cannot be blank" }
                require(version.isNotBlank()) { "Version cannot be blank" }
            }
        }

        data class VersionRange(
            @Input override val name: String,
            @Input override val url: String,
            @Input val from: String,
            @Input val to: String,
            @Input val inclusive: Boolean = true
        ) : Remote {
            init {
                require(name.isNotBlank()) { "Package name cannot be blank" }
                require(url.isNotBlank()) { "URL cannot be blank" }
                require(from.isNotBlank()) { "From version cannot be blank" }
                require(to.isNotBlank()) { "To version cannot be blank" }
            }
        }

        data class Branch(
            @Input override val name: String,
            @Input override val url: String,
            @Input val branchName: String
        ) : Remote {
            init {
                require(name.isNotBlank()) { "Package name cannot be blank" }
                require(url.isNotBlank()) { "URL cannot be blank" }
                require(branchName.isNotBlank()) { "Branch name cannot be blank" }
            }
        }

        data class FromVersion(
            @Input override val name: String,
            @Input override val url: String,
            @Input val version: String
        ) : Remote {
            init {
                require(name.isNotBlank()) { "Package name cannot be blank" }
                require(url.isNotBlank()) { "URL cannot be blank" }
                require(version.isNotBlank()) { "Version cannot be blank" }
            }
        }
    }
}
