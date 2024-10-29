package io.github.ttypic.swiftklib.gradle.api

import java.io.File

interface SwiftKlibEntry {
    var path: File

    var minIos: Int
    var minMacos: Int
    var minTvos: Int
    var minWatchos: Int

    fun packageName(name: String)

    @ExperimentalSwiftklibApi
    fun dependencies(configuration: SwiftPackageConfiguration.() -> Unit)

}
