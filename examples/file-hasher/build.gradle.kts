plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.4.0-beta04").apply(false)
    id("com.android.library").version("7.4.0-beta04").apply(false)
    kotlin("android").version("1.8.0").apply(false)
    kotlin("multiplatform").version("1.8.0").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
