package com.ttypic.swiftklib.gradle

import javax.inject.Inject


abstract class SwiftKlibEntry @Inject constructor(val name: String) {
    var targets: MutableSet<String> = mutableSetOf()
}