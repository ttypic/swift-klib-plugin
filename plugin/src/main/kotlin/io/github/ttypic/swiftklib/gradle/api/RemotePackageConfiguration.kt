package io.github.ttypic.swiftklib.gradle.api

@ExperimentalSwiftklibApi
interface RemotePackageConfiguration {
    /**
     * Sets GitHub repository as the package source.
     * Specifies the main package name in case of multi target package (ex: Firebase)
     */
    fun github(owner: String, repo: String, packageName: String? = null)

    /**
     * Sets custom URL as the package source.
     * Specifies the main package name in case of multi target package (ex: Firebase)
     */
    fun url(url: String, packageName: String? = null)

    /**
     * Specifies exact version of the package.
     */
    fun exactVersion(version: String)

    /**
     * Specifies version range for the package.
     */
    fun versionRange(
        from: String,
        to: String,
        inclusive: Boolean = true
    )

    /**
     * Specifies branch to use for the package.
     */
    fun branch(branchName: String)

    /**
     * Specifies minimum version of the package.
     */
    fun fromVersion(version: String)
}
