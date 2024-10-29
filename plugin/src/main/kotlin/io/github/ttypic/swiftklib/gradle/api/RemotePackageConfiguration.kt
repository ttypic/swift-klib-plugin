package io.github.ttypic.swiftklib.gradle.api

@ExperimentalSwiftklibApi
interface RemotePackageConfiguration {
    /**
     * Sets GitHub repository as the package source.
     */
    fun github(owner: String, repo: String)

    /**
     * Sets custom URL as the package source.
     */
    fun url(url: String)

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
