package io.github.ttypic.swiftklib.gradle.templates

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency
import org.gradle.process.ExecOperations
import java.io.File

internal fun createPackageSwiftContents(
    cinteropName: String,
    dependencies: Collection<SwiftPackageDependency>,
    minIos: String,
    minMacos: String,
    minTvos: String,
    minWatchos: String,
    toolsVersion: String,
): String = """
    // swift-tools-version: $toolsVersion
    import PackageDescription

    let package = Package(
        name: "$cinteropName",
        ${getPlatformBlock(minIos, minMacos, minTvos, minWatchos)},
        products: [
            .library(
                name: "$cinteropName",
                type: .static,
                targets: ${getProductsTargets(cinteropName)})
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
        ]
    )
""".trimIndent()

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
        dependencies.forEach { dependency ->
            add(dependency.toSwiftPackageDependencyDeclaration())
        }
    }.joinToString(",")
}

private fun SwiftPackageDependency.toSwiftPackageDependencyDeclaration(): String = when (this) {
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
}


private fun getDependenciesTargets(
    dependencies: Collection<SwiftPackageDependency>
): String {
    return buildList {
        dependencies.forEach { dependency ->
            dependency.name.forEach { library ->
                add(".product(name: \"${library}\", package: \"${dependency.packageName ?: library}\")")
            }
        }
    }.joinToString(",")
}

private fun getProductsTargets(cinteropName: String): String {
    return "[\"$cinteropName\"]"
}
