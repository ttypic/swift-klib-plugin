# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/ttypic/swift-klib-plugin/tree/HEAD)

## 0.6.0 2024-07-21

[Full Changelog](https://github.com/ttypic/swift-klib-plugin/compare/v0.5.1...v0.6.0)

**Merged pull requests:**

- Use Clang modules in cinterop instead of headers. [\#32](https://github.com/ttypic/swift-klib-plugin/pull/32)
- Prettify swift build output [\#29](https://github.com/ttypic/swift-klib-plugin/pull/29)
- Upgrade Gradle and dependencies. Migrate to version
  catalog. [\#31](https://github.com/ttypic/swift-klib-plugin/pull/31)

## 0.5.1 2023-11-24

[Full Changelog](https://github.com/ttypic/swift-klib-plugin/compare/v0.5.0...v0.5.1)

**Merged pull requests:**

- fix: more robust readXcodeMajorVersion [\#20](https://github.com/ttypic/swift-klib-plugin/pull/20)

## 0.5.0 2023-11-16

[Full Changelog](https://github.com/ttypic/swift-klib-plugin/compare/v0.4.0...v0.5.0)

**Merged pull requests:**

- Support Xcode being installed in a non-default
  path [\#17](https://github.com/ttypic/swift-klib-plugin/pull/17) ([hartbit](https://github.com/hartbit))
- fix: use platformVersion instead of min*Version linker flag for xcode
  15 [\#19](https://github.com/ttypic/swift-klib-plugin/pull/19)

## 0.4.0 2023-10-08

[Full Changelog](https://github.com/ttypic/swift-klib-plugin/compare/v0.3.0...v0.4.0)

**Closed issues:**

- Invalid Manifest \(xCode 15\) [\#11](https://github.com/ttypic/swift-klib-plugin/issues/11)

**Merged pull requests:**

- Workaround for wrong SDK in build process for
  Xcode15 [\#13](https://github.com/ttypic/swift-klib-plugin/pull/13) ([davidtaylor-juul](https://github.com/davidtaylor-juul))

## 0.3.0 2023-06-25

* Add tvOS, watchOS, macOS targets

## 0.2.1 2023-03-05

* Compile with Java 8 toolchain

## 0.2.0 2023-02-26

* **Fix:** add step to clean old build files in build directory
* Light refactoring of source code

## 0.1.0 2023-02-08

* Invalidate cinterop task if source Swift-files have changed

## 0.0.1 2022-12-27

* Initial release
