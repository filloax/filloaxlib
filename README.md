Simple Kotlin library for Minecraft Fabric that contains some handy functions for mods I worked in.

To use in your project use [JitPack](https://jitpack.io/#filloax/fx-lib), like this:

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
  implementation("com.github.filloax:fx-lib:Tag")
  // for example
  implementation("com.github.filloax:fx-lib:v0.28.1-1.21-common")
}
```

Guide is also in the JitPack link. The first time a version gets downloaded (globally) it will likely time out as
JitPack still needs to build it.

---

## Source code structure

Uses [Favouriteless's template](https://github.com/Favouriteless/ML-Template), with added kotlin mod stuff.
