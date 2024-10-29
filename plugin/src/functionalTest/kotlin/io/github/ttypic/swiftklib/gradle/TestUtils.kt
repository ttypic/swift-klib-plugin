package io.github.ttypic.swiftklib.gradle

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.OS

fun assumeMacos() {
    assumeTrue(OS.MAC.isCurrentOs)
}

fun assumeLinux() {
    assumeTrue(OS.LINUX.isCurrentOs)
}
