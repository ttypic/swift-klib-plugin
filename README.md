<img src="https://github.com/ttypic/swift-klib-plugin/raw/main/docs/media/swiftklib-light.svg" alt="Swift Klib library logo" width="300">

# Swift Klib Gradle Plugin

![badge-ios](https://img.shields.io/badge/platform-ios-light)
![badge-mac](https://img.shields.io/badge/platform-macos-light)
![badge-tvos](https://img.shields.io/badge/platform-tvos-light)
![badge-watchos](https://img.shields.io/badge/platform-watchos-light)

This gradle plugin provides easy way to include your Swift source files in your **Kotlin Multiplatform Mobile**
shared module and access them in Kotlin via `cinterop` for iOS targets. It is useful for:

* Accessing Swift-only libraries _(e.g. CryptoKit)_
* Creating a Kotlin-Friendly Swift API
* Learning how Swift <-> Kotlin interoperability works

**Note:** _Plugin has been tested on Gradle 7.5+, Xcode 15+_

## Installation

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):

```kotlin
plugins {
    id("io.github.ttypic.swiftklib") version "0.6.0"
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):

```kotlin
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("io.github.ttypic:plugin:0.6.0")
  }
}

apply(plugin = "io.github.ttypic.swiftklib")
```

## Usage

Plugin works together with [Kotlin Multiplatform plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.multiplatform).

### Prepare Swift code

Place your Swift files inside your project in separate folder (e.g. `native/HelloSwift`).

Make sure that Swift API you want to expose to Kotlin is [compatible with Objective-C](https://lazarevzubov.medium.com/compatible-with-objective-c-swift-code-e7c3239d949).

```swift
@objc public class HelloWorld : NSObject {
    @objc public class func helloWorld() -> String {
        return "HeLLo WorLd!"
    }
}
```

### Setup swiftklib extension

Then you need to add `cinterop` for target platforms in your **Kotlin Multiplatform Plugin**. There is
no need to configure it or add `.def` file, all configuration will be done automatically by **Swift Klib**.

```kotlin
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("HelloSwift")
                }
            }
        }
    }

    //...
}
```

And finally provide settings for it in `swiftklib` extension.
You need to specify `path` and `packageName` parameters.

```kotlin
swiftklib {
    create("HelloSwift") {
        path = file("native/HelloSwift")
        packageName("com.ttypic.objclibs.greeting")
    }
}
```

### Examples

More samples can be found in the [examples/](https://github.com/ttypic/swift-klib-plugin/tree/main/examples) folder.

## License

This package is licensed under MIT license.
