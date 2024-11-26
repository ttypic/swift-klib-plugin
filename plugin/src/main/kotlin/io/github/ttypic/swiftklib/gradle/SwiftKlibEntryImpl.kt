package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.SwiftKlibEntry
import io.github.ttypic.swiftklib.gradle.api.SwiftPackageConfiguration
import io.github.ttypic.swiftklib.gradle.internal.SwiftPackageConfigurationImpl
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal abstract class SwiftKlibEntryImpl @Inject constructor(
    val name: String,
    private val objects: ObjectFactory,
) : SwiftKlibEntry {
    val _path: Property<File> = objects.property(File::class.java)
    val _packageName: Property<String> = objects.property(String::class.java)
    val _toolsVersion: Property<String> = objects.property(String::class.java).convention("5.9")
    val _minIos: Property<String> = objects.property(String::class.java).convention("12.0")
    val _minMacos: Property<String> = objects.property(String::class.java).convention("10.13")
    val _minTvos: Property<String> = objects.property(String::class.java).convention("12.0")
    val _minWatchos: Property<String> = objects.property(String::class.java).convention("4.0")
    override var path: File by _path.bind()
    override var minIos: String by _minIos.bind()
    override var minMacos: String by _minMacos.bind()
    override var minTvos: String by _minTvos.bind()
    override var minWatchos: String by _minWatchos.bind()
    override var toolsVersion: String by _toolsVersion.bind()

    internal val dependencyHandler = SwiftPackageConfigurationImpl(objects)

    override fun packageName(name: String) = _packageName.set(name)

    override fun dependencies(configuration: SwiftPackageConfiguration.() -> Unit) {
        dependencyHandler.apply(configuration)
    }
}

fun <T> Property<T>.bind(): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value)
        }
    }
}
