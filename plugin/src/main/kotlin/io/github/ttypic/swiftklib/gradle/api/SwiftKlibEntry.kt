package io.github.ttypic.swiftklib.gradle.api

import java.io.File

interface SwiftKlibEntry {
    var path: File

    var minIos: String
    var minMacos: String
    var minTvos: String
    var minWatchos: String
    var toolsVersion: String?

    fun packageName(name: String)

    @ExperimentalSwiftklibApi
    fun dependencies(configuration: SwiftPackageConfiguration.() -> Unit)

}
