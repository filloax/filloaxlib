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

Uses template [Paramita](https://github.com/3TUSK/Paramita) as base.

---

## Source code structure

- **shared** contains code that does not depend on a specific Minecraft version (and so, Minecraft classes in general).
  So, classes that are only used by other modules without referencing MC directly.
- **\<version>-base** includes non mod-loader specific code that ideally should contain as much logic as possible
- **\<version>-\<loader>** contains loader specific code.