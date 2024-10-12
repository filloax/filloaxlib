package com.filloax.fxlib.api.json

import com.filloax.fxlib.api.concatIterators
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import org.apache.commons.lang3.SerializationException
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.IOException
import java.lang.StringBuilder

/**
 * Similar to [net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener] but using Kotlin Json
 * @param enableJsonc Allow comments and trailing commas in the file
 */
abstract class KotlinJsonResourceReloadListener(
    private val json: Json,
    private val directory: String,
    private val enableJsonc: Boolean = true,
) : SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>>() {

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    override fun prepare(resourceManager: ResourceManager, profiler: ProfilerFiller): Map<ResourceLocation, JsonElement> {
        val map = HashMap<ResourceLocation, JsonElement>()
        scanDirectory(resourceManager, directory, json, map, enableJsonc)
        return map
    }

    companion object {
        private val LOGGER = LogManager.getLogger()

        fun scanDirectory(
            resourceManager: ResourceManager, dirPath: String, json: Json, map: MutableMap<ResourceLocation, JsonElement>,
            enableJsonc: Boolean = true,
        ) {
            val fileToIdConverter = FileToIdConverter.json(dirPath)
            val jsoncF2IdConverter = FileToIdConverter(dirPath, ".jsonc")
            val resources = if (enableJsonc)
                    concatIterators(
                        fileToIdConverter.listMatchingResources(resourceManager).iterator(),
                        jsoncF2IdConverter.listMatchingResources(resourceManager).iterator(),
                    )
                else
                    fileToIdConverter.listMatchingResources(resourceManager).iterator()
            for ((resourceLocation, value) in resources) {
                val fileIdentifier = fileToIdConverter.fileToId(resourceLocation)
                try {
                    val fileInput = value.openAsReader()
                    try {
                        val text = if (enableJsonc) makeStrictJson(fileInput) else fileInput.readText()
                        val jsonElement = json.decodeFromString<JsonElement>(text)
                        map.put(fileIdentifier, jsonElement) ?: continue
                        throw IllegalStateException("Duplicate data file ignored with ID $fileIdentifier")
                    } finally {
                        if (fileInput == null) continue
                        fileInput.close()
                    }
                } catch (exception: SerializationException) {
                    LOGGER.error("Couldn't parse data file {} from {}", fileIdentifier, resourceLocation, exception)
                } catch (exception: IOException) {
                    LOGGER.error("Couldn't parse data file {} from {}", fileIdentifier, resourceLocation, exception)
                } catch (exception: IllegalArgumentException) {
                    LOGGER.error("Couldn't parse data file {} from {}", fileIdentifier, resourceLocation, exception)
                }
            }
        }

        // Removes comments and trailing commas
        private fun makeStrictJson(fileInput: BufferedReader): String {
            val textBuilder = StringBuilder()

            fileInput.forEachLine { line ->
                // Remove comments from each line (comments start with // in JSONC files)
                val lineWithoutComments = line.replace(Regex("//.*"), "").trim()

                if (lineWithoutComments.isNotEmpty()) {
                    textBuilder.appendLine(lineWithoutComments)
                }
            }

            val text = textBuilder.toString()

            return text.replace(Regex(",\\s*}", RegexOption.MULTILINE), "}")
                .replace(Regex(",\\s*\\]", RegexOption.MULTILINE), "]")
        }
    }
}

