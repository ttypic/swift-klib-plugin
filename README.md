<picture>
  <source media="(prefers-color-scheme: light)" srcset="docs/media/swiftklib-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="docs/media/swiftklib-dark.svg">
  <img src="docs/media/swiftklib-dark.svg" width="300">
</picture>

<br/>

# Swift Klib Gradle Plugin

This gradle plugin provides easy way to include your Swift source files in your **Kotlin Multiplatform Mobile**
shared module and access them in Kotlin via `cinterop` for iOS targets. It is useful for:

* Accessing Swift-only libraries _(e.g. CryptoKit)_
* Creating a Kotlin-Friendly Swift API
* Learning how Swift <-> Kotlin interoperability works

**Note:** _This plugin is still under development, it works quite well for my private projects. But
it hasn't been tested in different environments and setups. If you like the idea of the project and
have trouble with setup, don't hesitate and create issue or start a discussion. Any feedback is
very much appreciated._

**Note:** _Plugin has been tested on Gradle 7.5+, Xcode 13+_

## Installation

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):

```kotlin
plugins {
    id("io.github.ttypic.swiftklib") version "0.1.0"
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
    classpath("io.github.ttypic:plugin:0.1.0")
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

More samples can be found in the [example/](https://github.com/ttypic/swift-klib-plugin/tree/main/example) folder.

## License

This package is licensed under MIT license.
