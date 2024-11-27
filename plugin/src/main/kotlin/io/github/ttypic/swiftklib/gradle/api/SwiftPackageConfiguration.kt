package io.github.ttypic.swiftklib.gradle.api

@ExperimentalSwiftklibApi
interface SwiftPackageConfiguration {
    /**
     * Configures a local package dependency.
     * @param name Package name
     * @param path Local path to the package
     */
    fun local(name: String, path: java.io.File)

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

