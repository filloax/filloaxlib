import INCLUDE_LIBS
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtraPropertiesExtension
import java.lang.Runtime.Version

fun Project.utils(versionCatalogs: VersionCatalogsExtension, ext: ExtraPropertiesExtension)
    = ProjectUtils(this, versionCatalogs, ext)

/**
 * Wrapper around some gradle stuff as there's no sane way to make utils that use
 * Gradle Kotlin DSL extension methods
 */
class ProjectUtils(
    private val project: Project,
    versionCatalogs: VersionCatalogsExtension,
    private val ext: ExtraPropertiesExtension,
) {
    private val libs = versionCatalogs.find("libs").get()

    val includeLibs = INCLUDE_LIBS.map { libs.findLibrary(it).orElseThrow() }
}