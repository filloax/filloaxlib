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
  implementation("com.github.filloax:fx-lib:v0.10.0-1.20.4-fabric")
}
```

Guide is also in the JitPack link. The first time a version gets downloaded (globally) it will likely time out as
JitPack still needs to build it.

Uses template [Paramita](https://github.com/3TUSK/Paramita) as base, and also Botania's structure for
cross-loader development. Feel free to use this as a Kotlin multi-version/loader minecraft mod project template, 
even if it's not comprehensive at the moment (but adding new versions should be simple enough).

---

## Source code structure

- **shared** contains code that does not depend on a specific Minecraft version (and so, Minecraft classes in general).
  So, classes that are only used by other modules without referencing MC directly.
- **\<version>-base** includes non mod-loader specific code that ideally should contain as much logic as possible
- **\<version>-\<loader>** contains loader specific code.

Use java 21 as the gradle JVM.

## TODO

Internally: rework the multi-project layout to use a common 
plugin to configure subprojects instead of a huge subprojects {}
block in the root build.gradle.kts
