<picture>
  <source media="(prefers-color-scheme: light)" srcset="docs/media/swiftklib-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="docs/media/swiftklib-dark.svg">
  <img src="docs/media/logo/titled-1.png" width="300">
</picture>

<br/>

# Swift Klib Gradle Plugin

This gradle plugin is aimed to provide easy way to generate Kotlin binding for
Swift-only API __(e.g. CryptoKit)__ and use it later in **Kotlin Multiplatform Mobile** shared library.

**Note:** __This plugin is still under development, it works quite well for my private projects. But
it hasn't been tested in different environments and setups. If you like the idea of the project and
have trouble with setup, don't hesitate and create issue or start a discussion. Any feedback is
very much appreciated.__

## Usage

Plugin works together with [Kotlin Multiplatform plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.multiplatform).


