package io.github.ttypic.swiftklib.gradle

import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject


abstract class SwiftKlibEntry @Inject constructor(val name: String) {

    abstract val pathProperty: Property<File>
    abstract val packageNameProperty: Property<String>
    abstract val minIosProperty: Property<Int>

    var path: File
        get() = pathProperty.get()
        set(value) {
            pathProperty.set(value)
        }

    fun packageName(name: String) = packageNameProperty.set(name)

    var minIos: Int
        get() = minIosProperty.get()
        set(value) {
            minIosProperty.set(value)
        }

}
