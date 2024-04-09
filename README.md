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
        implementation 'com.github.filloax:fx-lib:Tag'
}
```

Guide is also in the JitPack link.