package io.github.ttypic.swiftklib.gradle.templates

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import org.gradle.process.ExecOperations
import java.io.File

internal fun createPackageSwiftContents(
    cinteropName: String,
    dependencies: Collection<SwiftPackageDependency>,
    execOperations: ExecOperations,
    swiftBuildDir: File,
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String,
    toolsVersion: String?,
) {
    execOperations.exec {
        it.executable = "swift"
        it.workingDir = swiftBuildDir
        it.isIgnoreExitValue = true
        it.args = listOf(
            "package",
            "init",
            "--name",
            cinteropName,
            "--type",
            "empty",
            "--disable-xctest",
            "--disable-swift-testing"
        )
    }.run {
        if (exitValue != 0) {
            throw RuntimeException("Failed to init Swift Package")
        }
    }

    addMissingBlock(cinteropName, swiftBuildDir, minIos, minMacos, minTvos, minWatchos)

    if (!toolsVersion.isNullOrEmpty()) {
        execOperations.exec {
            it.executable = "swift"
            it.workingDir = swiftBuildDir
            it.isIgnoreExitValue = true
            it.args = listOf(
                "package",
                "tools-version",
                "--set",
                toolsVersion
            )
        }.run {
            if (exitValue != 0) {
                throw RuntimeException("Failed to set the tool version $toolsVersion")
            }
        }
    }

    dependencies.forEach { dependency ->
        when (dependency) {
            is SwiftPackageDependency.Local, is SwiftPackageDependency.Remote -> {
                null
            }

            is SwiftPackageDependency.LocalBinary -> {
                execOperations.exec {
                    it.executable = "sed"
                    it.workingDir = swiftBuildDir
                    val escapedPath =
                        dependency.path.relativeTo(swiftBuildDir).toString().replace("/", "\\/")
                    it.args = listOf(
                        "-i",
                        "''",
                        "/targets: \\[/,/]/ s/]/    \\n\\t.binaryTarget(name: \"${dependency.name.first()}\", path:\"${escapedPath}\"),\\n]/",
                        "Package.swift"
                    )
                    it.isIgnoreExitValue = true
                }
            }

            is SwiftPackageDependency.RemoteBinary -> {
                execOperations.exec {
                    it.executable = "sed"
                    it.workingDir = swiftBuildDir
                    val escapedURL = dependency.url.toString().replace("/", "\\/")
                    it.args = buildList {
                        add("-i")
                        add("''")
                        if (dependency.checksum != null) {
                            add("/targets: \\[/,/]/ s/]/    \\n\\t.binaryTarget(name: \"${dependency.name.first()}\", url:\"${escapedURL}\", checksum:\"${dependency.checksum}\"),\\n]/")
                        } else {
                            add("/targets: \\[/,/]/ s/]/    \\n\\t.binaryTarget(name: \"${dependency.name.first()}\", url:\"${escapedURL}\", checksum:\"null\"),\\n]/")
                        }
                        add("Package.swift")
                    }
                    it.isIgnoreExitValue = true
                }
            }
        }?.run {
            if (exitValue != 0) {
                throw RuntimeException("Failed to add Swift Package target $cinteropName")
            }
        }
    }

    execOperations.exec {
        it.executable = "swift"
        it.workingDir = swiftBuildDir
        it.args = buildList {
            add("package")
            add("add-target")
            add(cinteropName)
            add("--path")
            add(cinteropName)
            dependencies.forEach { dependency ->
                if (dependency.isBinaryDependency) {
                    add("--dependencies")
                    add(dependency.packageName ?: dependency.name.first())
                }
            }
        }
        it.isIgnoreExitValue = true
    }.run {
        if (exitValue != 0) {
            throw RuntimeException("Failed to add Swift Package target $cinteropName")
        }
    }


    dependencies.forEach { dependency ->
        when (dependency) {
            is SwiftPackageDependency.Local -> {
                val escapedPath = dependency.path.absolutePath.replace("/", "\\/")
                execOperations.exec {
                    it.executable = "sed"
                    it.workingDir = swiftBuildDir
                    it.args = listOf(
                        "-i",
                        "''",
                        """
                       /dependencies: \[/a\
\        .package(path: \"${escapedPath}\"),

                        """,
                        "Package.swift"
                    )
                    it.isIgnoreExitValue = true
                }
            }

            is SwiftPackageDependency.Remote -> {
                execOperations.exec {
                    it.executable = "swift"
                    it.workingDir = swiftBuildDir
                    it.args = listOf("package", "add-dependency") + dependency.toSwiftArgs()
                    it.isIgnoreExitValue = true
                }
            }

            else -> {
                null
            }
        }?.run {
            if (exitValue != 0) {
                throw RuntimeException(
                    "Failed to add Swift Package dependency $dependency",
                )
            }
        }
    }

    dependencies.forEach { dependency ->
        if (dependency is SwiftPackageDependency.Local || dependency is SwiftPackageDependency.Remote) {
            dependency.name.forEach { library ->
                execOperations.exec {
                    it.executable = "swift"
                    it.workingDir = swiftBuildDir
                    it.isIgnoreExitValue = true
                    it.args = listOf(
                        "package",
                        "add-target-dependency",
                        library,
                        "--package",
                        dependency.packageName ?: library,
                        cinteropName,
                    )
                }.run {
                    if (exitValue != 0) {
                        throw RuntimeException(
                            "Failed to add Swift Package target dependency $cinteropName - package = ${dependency.packageName ?: library}:$library",
                        )
                    }
                }
            }
        }
    }

    execOperations.exec {
        it.executable = "swift"
        it.workingDir = swiftBuildDir
        it.isIgnoreExitValue = true
        it.args = buildList<String> {
            add("package")
            add("add-product")
            add("--targets")
            add(cinteropName)
            dependencies.forEach { dependency ->
                if (dependency.isBinaryDependency) {
                    add("--targets")
                    add(dependency.packageName ?: dependency.name.first())
                }
            }
            add("--type")
            add("static-library")
            add(cinteropName)
        }
    }.run {
        if (exitValue != 0) {
            throw RuntimeException(
                "Failed to add Swift Package library $cinteropName",
            )
        }
    }
}


internal fun SwiftPackageDependency.toSwiftArgs(): List<String> = when (this) {
    is SwiftPackageDependency.Local,
    is SwiftPackageDependency.LocalBinary,
    is SwiftPackageDependency.RemoteBinary ->
        emptyList()

    is SwiftPackageDependency.Remote.ExactVersion ->
        listOf(url, "--exact", version)

    is SwiftPackageDependency.Remote.VersionRange -> {
        if (inclusive) {
            // can't do inclusive range from command line
            // but I think it's better to use up-to-next-minor-from
            // it needs to be rethink
            listOf(url, "--up-to-next-minor-from", from)
        } else {
            listOf(url, "--from", from, "--to", to)
        }
    }

    is SwiftPackageDependency.Remote.Branch ->
        listOf(url, "--branch", branchName)

    is SwiftPackageDependency.Remote.FromVersion ->
        listOf(url, "--from", version)
}

private fun addMissingBlock(
    name: String,
    swiftBuildDir: File,
    minIos: String,
    minMacos: String,
    minTvos: String, minWatchos: String
) {
    File(swiftBuildDir, "Package.swift").readText().run {
        val platformContent = buildPlatformContent(minIos, minMacos, minTvos, minWatchos)
        val updated =
            replace(
                "name: \"$name\"\n",
                "name: \"$name\",\n\tplatforms: [${platformContent}],\n\tproducts: [\n],\n\tdependencies: [\n],\n\ttargets: [\n]\n"
            )
        File(swiftBuildDir, "Package.swift").writeText(updated)
    }
}

private fun buildPlatformContent(
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String
): String {
    return listOfNotNull(
        ".iOS(\"$minIos\")".takeIf { minIos.isNotEmpty() },
        ".macOS(\"$minMacos\")".takeIf { minMacos.isNotEmpty() },
        ".tvOS(\"$minTvos\")".takeIf { minTvos.isNotEmpty() },
        ".watchOS(\"$minWatchos\")".takeIf { minWatchos.isNotEmpty() },
    ).joinToString(",")
}

private val SwiftPackageDependency.isBinaryDependency: Boolean
    get() = (this is SwiftPackageDependency.LocalBinary) || (this is SwiftPackageDependency.RemoteBinary)
