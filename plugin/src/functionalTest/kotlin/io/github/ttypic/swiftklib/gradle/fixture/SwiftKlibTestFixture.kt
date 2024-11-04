package io.github.ttypic.swiftklib.gradle.fixture

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.Source
import com.autonomousapps.kit.Subproject
import com.autonomousapps.kit.gradle.Plugin
import io.github.ttypic.swiftklib.gradle.api.RemotePackageConfiguration
import io.github.ttypic.swiftklib.gradle.api.SwiftKlibEntry
import io.github.ttypic.swiftklib.gradle.api.SwiftPackageConfiguration
import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SwiftKlibTestFixture private constructor(
    protected val configuration: TestConfiguration
) : AbstractGradleProject() {
    private var _gradleProject: GradleProject? = null

    val gradleProject: GradleProject
        get() = _gradleProject ?: createProject().also { _gradleProject = it }

    data class TestConfiguration(
        val swiftklibName: String = "test",
        val swiftklibPackage: String = "test",
        val swiftSources: List<SwiftSource> = emptyList(),
        val kotlinSources: List<KotlinSource> = emptyList(),
        val additionalConfig: String = "",
        val dslKind: GradleProject.DslKind = GradleProject.DslKind.KOTLIN,
        val pluginVersion: String = System.getProperty("com.autonomousapps.plugin-under-test.version"),
        internal val configurationBlock: SwiftKlibEntry.() -> Unit = {}
    )

    class Builder {
        private var config = TestConfiguration()

        fun withName(name: String) = apply {
            config = config.copy(swiftklibName = name)
        }

        fun withSwiftSources(vararg sources: SwiftSource) = apply {
            config = config.copy(swiftSources = sources.toList())
        }

        fun withKotlinSources(vararg sources: KotlinSource) = apply {
            config = config.copy(kotlinSources = sources.toList())
        }

        fun withDslKind(dslKind: GradleProject.DslKind) = apply {
            config = config.copy(dslKind = dslKind)
        }

        fun withConfiguration(block: SwiftKlibEntry.() -> Unit) = apply {
            config = config.copy(configurationBlock = block)
        }

        fun withDefaultConfiguration(block: SwiftKlibEntry.() -> Unit) = apply {
            config = config.copy(configurationBlock = block)
        }

        fun build(): SwiftKlibTestFixture = object : SwiftKlibTestFixture(config) {
            override fun createProject(): GradleProject = createDefaultProject()
        }
    }

    protected abstract fun createProject(): GradleProject

    protected fun createDefaultProject(): GradleProject {
        val entry = TestSwiftKlibEntryImpl()
        configuration.configurationBlock(entry)

        return newGradleProjectBuilder(configuration.dslKind)
            .withRootProject {
                withFile(
                    "gradle.properties",
                    "kotlin.mpp.enableCInteropCommonization=true"
                )
            }
            .withSubproject("library") {
                setupSources()
                setupGradleConfig(entry)
            }
            .write()
    }

    private fun Subproject.Builder.setupSources() {
        // Setup Swift sources
        configuration.swiftSources.forEach { source ->
            withFile(
                "${configuration.swiftklibName}/src/main/swift/${source.filename}",
                source.content
            )
        }

        // Setup Kotlin sources
        val kotlinSources = if (configuration.kotlinSources.isEmpty()) {
            listOf(KotlinSource.default())
        } else {
            configuration.kotlinSources
        }

        kotlinSources.forEach { source ->
            sources.add(
                Source.kotlin(source.content)
                    .withPath(source.packageName, source.className)
                    .build()
            )
        }
    }

    private fun Subproject.Builder.setupGradleConfig(entry: TestSwiftKlibEntryImpl) {
        withBuildScript {
            plugins(
                Plugin.kotlinMultiplatform,
                Plugin("io.github.ttypic.swiftklib", configuration.pluginVersion)
            )

            withKotlin(createKotlinBlock(entry))
        }
    }

    private fun createKotlinBlock(entry: TestSwiftKlibEntryImpl): String {
        val configBlock = buildString {
            appendLine("swiftklib {")
            appendLine("    create(\"${configuration.swiftklibName}\") {")
            appendLine("        path = file(\"${configuration.swiftklibName}/src/main/swift\")")

            appendLine("        packageName(\"${configuration.swiftklibPackage}\")")

            // Only add minimum version configurations if they differ from defaults
            if (entry._minIos.hasValue()) {
                appendLine("        minIos = \"${entry.minIos}\"")
            }
            if (entry._minMacos.hasValue()) {
                appendLine("        minMacos = \"${entry.minMacos}\"")
            }
            if (entry._minTvos.hasValue()) {
                appendLine("        minTvos = \"${entry.minTvos}\"")
            }
            if (entry._minWatchos.hasValue()) {
                appendLine("        minWatchos = \"${entry.minWatchos}\"")
            }

            if (entry.dependencies.isNotEmpty()) {
                appendLine("        dependencies {")
                entry.dependencies.forEach { dep ->
                    appendLine("            ${dep.toConfigString()}")
                }
                appendLine("        }")
            }

            appendLine("    }")
            appendLine("}")
        }

        return """
        @file:OptIn(io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi::class)
        kotlin {
            compilerOptions {
                optIn.addAll("kotlinx.cinterop.ExperimentalForeignApi")
            }

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach {
                it.compilations {
                    val main by getting {
                        cinterops.create("${configuration.swiftklibName}")
                    }
                }
            }

            ${configuration.additionalConfig}
        }

        $configBlock
        """.trimIndent()
    }

    companion object {
        fun builder() = Builder()
    }
}

val Plugin.Companion.kotlinMultiplatform
    get() = Plugin.of("org.jetbrains.kotlin.multiplatform", "2.0.21")


private class TestSwiftKlibEntryImpl : SwiftKlibEntry {
    val _path = notNull<File>()
    val _minIos = notNull<String>()
    val _minMacos = notNull<String>()
    val _minTvos = notNull<String>()
    val _minWatchos = notNull<String>()

    override var path: File by _path
    override var minIos: String by _minIos
    override var minMacos: String by _minMacos
    override var minTvos: String by _minTvos
    override var minWatchos: String by _minWatchos

    val dependencies = mutableListOf<TestDependencyConfig>()

    override fun packageName(name: String) {
        TODO("Package name changing in tests is not supported yet")
    }

    override fun dependencies(configuration: SwiftPackageConfiguration.() -> Unit) {
        val config = TestSwiftPackageConfigurationImpl()
        config.configuration()
        dependencies.addAll(config.dependencies)
    }
}


private class TestSwiftPackageConfigurationImpl : SwiftPackageConfiguration {
    internal val dependencies = mutableListOf<TestDependencyConfig>()

    override fun local(name: String, path: File) {
        dependencies.add(TestDependencyConfig.Local(name, path))
    }

    override fun remote(name: String, configuration: RemotePackageConfiguration.() -> Unit) {
        remote(listOf(name), configuration)
    }

    override fun remote(name: List<String>, configuration: RemotePackageConfiguration.() -> Unit) {
        val config = TestRemotePackageConfigurationImpl(name)
        config.configuration()
        dependencies.add(config.build())
    }
}

private class TestRemotePackageConfigurationImpl(private val name: List<String>) :
    RemotePackageConfiguration {

    private var url: String? = null
    private var packageName: String? = null
    private var versionConfig: TestVersionConfig? = null

    override fun github(owner: String, repo: String, packageName: String?) {
        url = "https://github.com/$owner/$repo.git"
        this.packageName = packageName
    }

    override fun url(url: String, packageName: String?) {
        this.url = url
        this.packageName = packageName
    }

    override fun exactVersion(version: String) {
        versionConfig = TestVersionConfig.Exact(version)
    }

    override fun versionRange(from: String, to: String, inclusive: Boolean) {
        versionConfig = TestVersionConfig.Range(from, to, inclusive)
    }

    override fun branch(branchName: String) {
        versionConfig = TestVersionConfig.Branch(branchName)
    }

    override fun fromVersion(version: String) {
        versionConfig = TestVersionConfig.From(version)
    }

    internal fun build(): TestDependencyConfig.Remote {
        return TestDependencyConfig.Remote(
            name = name,
            url = url,
            version = versionConfig,
            packageName = packageName
        )
    }
}

private sealed interface TestDependencyConfig {
    fun toConfigString(): String

    data class Local(val name: String, val path: File) : TestDependencyConfig {
        override fun toConfigString() = """local("$name", file("${path.absolutePath}"))"""
    }

    data class Remote(
        val name: List<String>,
        val url: String?,
        val version: TestVersionConfig?,
        val packageName: String?
    ) : TestDependencyConfig {
        override fun toConfigString() = buildString {
            if (name.size == 1) {
                append("remote(\"${name.first()}\") {\n")
            } else {
                append("remote(listOf(\"${name.joinToString("\",\"")}\")) {\n")
            }
            if (url != null) {
                if (packageName != null) {
                    append("                url(\"$url\", \"$packageName\")\n")
                } else {
                    append("                url(\"$url\")\n")
                }
            }
            if (version != null) {
                append("                ${version.toConfigString()}\n")
            }
            append("            }")
        }
    }
}

private sealed interface TestVersionConfig {
    fun toConfigString(): String

    data class Exact(val version: String) : TestVersionConfig {
        override fun toConfigString() = """exactVersion("$version")"""
    }

    data class Range(val from: String, val to: String, val inclusive: Boolean) : TestVersionConfig {
        override fun toConfigString() = """versionRange("$from", "$to", $inclusive)"""
    }

    data class Branch(val name: String) : TestVersionConfig {
        override fun toConfigString() = """branch("$name")"""
    }

    data class From(val version: String) : TestVersionConfig {
        override fun toConfigString() = """fromVersion("$version")"""
    }
}

private fun <T : Any> notNull() = NotNullVar<T>()

private class NotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
            ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun hasValue() = value != null

    public override fun toString(): String =
        "NotNullProperty(${if (value != null) "value=$value" else "value not initialized yet"})"
}
