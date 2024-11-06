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
        if (dependency is SwiftPackageDependency.Local) {
            addDependencyBlockIfNeeded(cinteropName, swiftBuildDir)
            val escapePath = dependency.path.absolutePath.replace("/", "\\/")
            execOperations.exec {
                it.executable = "sed"
                it.workingDir = swiftBuildDir
                it.args = listOf(
                    "-i",
                    "''",
                    "/dependencies: \\[/,/]/ s/]/    \\n\\t.package(path: \"${escapePath}\"),\\n]/",
                    "Package.swift"
                )
                it.isIgnoreExitValue = true
            }
        } else {
            execOperations.exec {
                it.executable = "swift"
                it.workingDir = swiftBuildDir
                it.args = listOf("package", "add-dependency") + dependency.toSwiftArgs()
                it.isIgnoreExitValue = true
            }
        }.run {
            if (exitValue != 0) {
                throw RuntimeException(
                    "Failed to add Swift Package dependency $dependency",
                )
            }
        }
    }
    addPlatformBlock(cinteropName, swiftBuildDir, minIos, minMacos, minTvos, minWatchos)
    execOperations.exec {
        it.executable = "swift"
        it.workingDir = swiftBuildDir
        it.args = listOf(
            "package",
            "add-target",
            "--path",
            cinteropName,
            cinteropName
        )
        it.isIgnoreExitValue = true
    }.run {
        if (exitValue != 0) {
            throw RuntimeException("Failed to add Swift Package target $cinteropName")
        }
    }

    dependencies.forEach { dependency ->
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

    execOperations.exec {
        it.executable = "swift"
        it.workingDir = swiftBuildDir
        it.isIgnoreExitValue = true
        it.args = listOf(
            "package",
            "add-product",
            "--targets",
            cinteropName,
            "--type",
            "static-library",
            cinteropName
        )
    }.run {
        if (exitValue != 0) {
            throw RuntimeException(
                "Failed to add Swift Package library $cinteropName",
            )
        }
    }

}


internal fun SwiftPackageDependency.toSwiftArgs(): List<String> = when (this) {
    is SwiftPackageDependency.Local ->
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

private fun addDependencyBlockIfNeeded(name: String, swiftBuildDir: File) {
    File(swiftBuildDir, "Package.swift").readText().run {
        if (!contains("dependencies:")) {
            val updated = replace("name: \"$name\"", "name: \"$name\",\n\tdependencies: []")
            File(swiftBuildDir, "Package.swift").writeText(updated)
        }
    }
}

private fun addPlatformBlock(
    name: String,
    swiftBuildDir: File,
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String
) {
    File(swiftBuildDir, "Package.swift").readText().run {
        if (!contains("platforms:")) {
            val entries = listOfNotNull(
                ".iOS(\"$minIos\")".takeIf { minIos.isNotEmpty() },
                ".macOS(\"$minMacos\")".takeIf { minMacos.isNotEmpty() },
                ".tvOS(\"$minTvos\")".takeIf { minTvos.isNotEmpty() },
                ".watchOS(\"$minWatchos\")".takeIf { minWatchos.isNotEmpty() },
            ).joinToString(",")
            if (entries.isNotEmpty()) {
                val updated =
                    replace(
                        "name: \"$name\",\n",
                        "name: \"$name\",\n\tplatforms: [$entries],\n"
                    )
                File(swiftBuildDir, "Package.swift").writeText(updated)
            }
        }
    }
}
