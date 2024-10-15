Simple Kotlin library for Minecraft Fabric that contains some handy functions for mods I worked in.


IMPORTANT: jitpack in current version doesn't seem to work properly, its version doesn't properly work in some inheritances (like FxSavedData)
while local versions do.

To use in your project use [JitPack](https://jitpack.io/#filloax/filloaxlib), like this:

1. Add Jitpack repository to build.gradle.

```kt
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add dependency to build.gradle

```kt
dependencies {
  implementation("com.github.filloax:filloaxlib:Tag")
  // for example
  implementation("com.github.filloax:filloaxlib:v0.30.0-1.21-fabric")
}
```

Guide is also in the JitPack link. The first time a version gets downloaded (globally) it will likely time out as
JitPack still needs to build it.

---

## Source code structure

Uses [Favouriteless's template](https://github.com/Favouriteless/ML-Template), with added kotlin mod stuff.