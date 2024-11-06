package io.github.ttypic.swiftklib.gradle.api

import java.net.URI

@ExperimentalSwiftklibApi
interface SwiftPackageConfiguration {
    /**
     * Configures a local package dependency.
     * @param name Package name
     * @param path Local path to the package
     */
    fun local(name: String, path: java.io.File)

    /**
     * Configures a local xcframework dependency
     * @param name Package name
     * @param path Local path to the xcframework (ex: /path/to/my.xcframework)
     */
    fun localBinary(name: String, path: java.io.File)

    /**
     * Configures a remote xcframework dependency
     * The xcframework folder need to be compressed
     * @param name Package name
     * @param url Remote url to the xcframework (ex: https://remote/my.xcframework.zip)
     * @param checksum Checksum of the xcframework
     */
    fun remoteBinary(name: String, url: URI, checksum: String? = null)

    /**
     * Configures a remote package dependency.
     * @param name the product's name to add
     * @param configuration Configuration block for the remote package
     */
    fun remote(name: String, configuration: RemotePackageConfiguration.() -> Unit)

    /**
     * Configures a remote package dependency.
     * @param names a list of product's name to add
     * @param configuration Configuration block for the remote package
     */
    fun remote(names: List<String>, configuration: RemotePackageConfiguration.() -> Unit)
}

