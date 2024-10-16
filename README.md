[![](https://jitpack.io/v/filloax/filloaxlib.svg)](https://jitpack.io/#filloax/filloaxlib)

<img src="logo.png" width="300px">

Simple Kotlin library for Minecraft Fabric that contains some handy functions for mods I worked in.

You can find Fabric and Neoforge versions on [Modrinth](https://modrinth.com/mod/filloaxlib).

Includes [kotlin-events](https://github.com/svby/kotlin-events) to use as a common event system in 
the loader-less modules in my mods.

To use in your project either use [JitPack](https://jitpack.io/#filloax/filloaxlib) or [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven), depending on if you want the 
common version (to use in the loader-less common project in cross-loader mods) or the loader-specific
version:

<details>
<summary>Jitpack (Common version)</summary>

You can also find the common versions in the [Releases](https://github.com/filloax/filloaxlib/releases) tab.

1. Add [JitPack](https://jitpack.io/#filloax/filloaxlib) repository to build.gradle.

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
  implementation("com.github.filloax.filloaxlib:filloaxlib-${loader}:Tag")
  // for example
  implementation("com.github.filloax.filloaxlib:filloaxlib-fabric:0.31.0-1.21")
}
```

Guide is also in the JitPack link. The first time a version gets downloaded (globally) it will likely time out as
JitPack still needs to build it.
</details>


<details>
<summary>Modrinth maven (Fabric/Neoforge)</summary>

TODO

</details>

---

## Source code structure

Uses [Favouriteless's template](https://github.com/Favouriteless/ML-Template), with added kotlin mod stuff.