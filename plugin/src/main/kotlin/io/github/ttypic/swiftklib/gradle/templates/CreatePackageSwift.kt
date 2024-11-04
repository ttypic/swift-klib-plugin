package io.github.ttypic.swiftklib.gradle.templates

import io.github.ttypic.swiftklib.gradle.SwiftPackageDependency

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
