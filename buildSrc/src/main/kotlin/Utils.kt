import gradle.kotlin.dsl.accessors._a3cefda71dba795f6746bc36999f0190.versionCatalogs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

val INCLUDE_LIBS = listOf(
    "kotlinevents",
)

val Project.includeLibs: List<Provider<MinimalExternalModuleDependency>>
    get() {
        val libs = versionCatalogs.find("libs").get()

        return INCLUDE_LIBS.map { libs.findLibrary(it).orElseThrow() }
    }