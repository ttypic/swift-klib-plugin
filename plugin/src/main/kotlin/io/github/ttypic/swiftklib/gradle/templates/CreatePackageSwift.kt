package io.github.ttypic.swiftklib.gradle.templates

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency

internal fun createPackageSwiftContents(
    cinteropName: String,
    dependencies: Collection<SwiftPackageDependency>
): String = """
    // swift-tools-version:5.6
    import PackageDescription

    let package = Package(
        name: "$cinteropName",
        products: [
            .library(
                name: "$cinteropName",
                type: .static,
                targets: ["$cinteropName"])
        ],
        dependencies: [
            ${dependencies.joinToString(",\n            ") { it.toSwiftPackageDeclaration() }}
        ],
        targets: [
            .target(
                name: "$cinteropName",
                dependencies: [
                    ${dependencies.joinToString(",\n                    ") { "\"${it.name}\"" }}
                ],
                path: "$cinteropName")
        ]
    )
""".trimIndent()


internal fun SwiftPackageDependency.toSwiftPackageDeclaration(): String = when (this) {
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
