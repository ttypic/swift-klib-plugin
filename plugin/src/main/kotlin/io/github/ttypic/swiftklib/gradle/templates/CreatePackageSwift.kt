package io.github.ttypic.swiftklib.gradle.templates

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import java.io.File

internal fun createPackageSwiftContents(
    cinteropName: String,
    dependencies: Collection<SwiftPackageDependency>,
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String,
    toolsVersion: String,
    swiftBuildDir: File
): String {
    var binaryDependencies = listOfNotNull(
        buildLocaleBinary(dependencies, swiftBuildDir).takeIf { it.isNotEmpty() },
        buildRemoteBinary(dependencies).takeIf { it.isNotEmpty() },
    ).joinToString(",")
    if (binaryDependencies.isNotEmpty()) {
        binaryDependencies = ",$binaryDependencies"
    }

    return """
    // swift-tools-version: $toolsVersion
    import PackageDescription

    let package = Package(
        name: "$cinteropName",
        ${getPlatformBlock(minIos, minMacos, minTvos, minWatchos)},
        products: [
            .library(
                name: "$cinteropName",
                type: .static,
                targets: [${getProductsTargets(cinteropName, dependencies)}])
        ],
        dependencies: [
            ${getDependencies(dependencies)}
        ],
        targets: [
            .target(
                name: "$cinteropName",
                dependencies: [
                    ${getDependenciesTargets(dependencies)}
                ],
                path: "$cinteropName")
            $binaryDependencies
        ]

    )
""".trimIndent()
}

private fun getPlatformBlock(
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String
): String {
    val entries = listOfNotNull(
        ".iOS(\"$minIos\")".takeIf { minIos.isNotEmpty() },
        ".macOS(\"$minMacos\")".takeIf { minMacos.isNotEmpty() },
        ".tvOS(\"$minTvos\")".takeIf { minTvos.isNotEmpty() },
        ".watchOS(\"$minWatchos\")".takeIf { minWatchos.isNotEmpty() },
    ).joinToString(",")
    return "platforms: [$entries]"
}

private fun getDependencies(dependencies: Collection<SwiftPackageDependency>): String {
    return buildList {
        dependencies
            .filter { !it.isBinaryDependency }
            .forEach { dependency ->
                dependency.toSwiftPackageDependencyDeclaration()?.let {
                    add(it)
                }
            }
    }.joinToString(",")
}

private fun getProductsTargets(
    cinteropName: String,
    dependencies: Collection<SwiftPackageDependency>
): String {
    return buildList {
        add("\"$cinteropName\"")
        dependencies
            .filter { it.isBinaryDependency }
            .forEach { dependency ->
                add("\"${dependency.packageName ?: dependency.name.first()}\"")
            }
    }.joinToString(",")
}

private fun SwiftPackageDependency.toSwiftPackageDependencyDeclaration(): String? = when (this) {
    is SwiftPackageDependency.Local ->
        """
        .package(path: "${path.absolutePath}")
        """.trimIndent()

    is SwiftPackageDependency.Remote.ExactVersion ->
        """
        .package(url: "$url", exact: "$version")
        """.trimIndent()

    is SwiftPackageDependency.Remote.VersionRange -> {
        val operator = if (inclusive) "..." else "..<"
        """
        .package(url: "$url", "$from"$operator"$to")
        """.trimIndent()
    }

    is SwiftPackageDependency.Remote.Branch ->
        """
        .package(url: "$url", branch: "$branchName")
        """.trimIndent()

    is SwiftPackageDependency.Remote.FromVersion ->
        """
        .package(url: "$url", from: "$version")
        """.trimIndent()

    is SwiftPackageDependency.LocalBinary -> null
    is SwiftPackageDependency.RemoteBinary -> null
}


private fun getDependenciesTargets(
    dependencies: Collection<SwiftPackageDependency>,
): String {
    return buildList {
        dependencies
            .forEach { dependency ->
                if (dependency.isBinaryDependency) {
                    add("\"${dependency.packageName ?: dependency.name.first()}\"")
                } else {
                    dependency.name.forEach { library ->
                        add(".product(name: \"${library}\", package: \"${dependency.packageName ?: library}\")")
                    }
                }
            }
    }.joinToString(",\n")
}

private fun buildLocaleBinary(
    dependencies: Collection<SwiftPackageDependency>,
    swiftBuildDir: File
): String {
    return buildList {
        dependencies
            .filterIsInstance<SwiftPackageDependency.LocalBinary>()
            .forEach { dependency ->
                // package path MUST be relative to somewhere, let's choose the swiftBuildDir
                val path = dependency.path.relativeTo(swiftBuildDir).toString()
                add(".binaryTarget(name: \"${dependency.name.first()}\", path:\"${path}\")")
        }
    }.joinToString(",\n")
}

private fun buildRemoteBinary(dependencies: Collection<SwiftPackageDependency>): String {
    return buildList {
        dependencies
            .filterIsInstance<SwiftPackageDependency.RemoteBinary>()
            .forEach { dependency ->
                // checksum is MANDATORY
                add(".binaryTarget(name: \"${dependency.name.first()}\", url:\"${dependency.url}\", checksum:\"${dependency.checksum}\")")
        }
    }.joinToString(",\n")
}

private val SwiftPackageDependency.isBinaryDependency: Boolean
    get() = (this is SwiftPackageDependency.LocalBinary) || (this is SwiftPackageDependency.RemoteBinary)
