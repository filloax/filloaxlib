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
// this uses the kotlin DSL, adapt it if you use a classic Groovy build.gradle
repositories {
  maven("https://jitpack.io")
}
```

2. Add dependency to build.gradle

```kt
// this uses the kotlin DSL, adapt it if you use a classic Groovy build.gradle
dependencies {
  implementation("com.github.filloax.filloaxlib:filloaxlib-common:tag")
  // for example
  implementation("com.github.filloax.filloaxlib:filloaxlib-common:0.31.0-1.21")
}
```
</details>


<details>
<summary>Modrinth maven (Fabric/Neoforge)</summary>

The recommended way to depend on the loader-specific versions of the mod is using [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven).

1. Add the Maven repository to build.gradle.

```kt
// this uses the kotlin DSL, adapt it if you use a classic Groovy build.gradle
repositories {
  exclusiveContent {
    forRepository {
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
        }
    }
    filter { includeGroup("maven.modrinth") }
  }
  // this is needed for kotlinevents, see below
  maven("https://jitpack.io")
}
```

2. Add dependency to build.gradle (including compile libraries)

```kt
// this uses the kotlin DSL, adapt it if you use a classic Groovy build.gradle
dependencies {
  implementation("maven.modrinth:filloaxlib:<version>-<loader>")
  // for example
  implementation("maven.modrinth:filloaxlib:0.31.0-1.21-neoforge")
  // loom notation
  modImplementation("maven.modrinth:filloaxlib:0.31.0-1.21-fabric")

  // this is needed only with the loader-specific modules until I find out how
  // to fix it
  compileOnly("com.github.stuhlmeier:kotlin-events:v2.0")
}
```

Additionally, if you want to include the jar for this library, you'll either need to
also include kotlin-events or use shadow jar or similar plugins to include transitive
dependencies.

</details>

---

## Source code structure

Uses [Favouriteless's template](https://github.com/Favouriteless/ML-Template), with added kotlin mod stuff.
