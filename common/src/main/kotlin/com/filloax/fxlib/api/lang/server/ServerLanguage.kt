package com.filloax.fxlib.api.lang.server

import com.filloax.fxlib.Constants
import com.filloax.fxlib.InternalUtils.resLoc
import com.filloax.fxlib.api.json.KotlinJsonResourceReloadListener
import com.filloax.fxlib.lang.server.LanguageInfo
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import net.minecraft.util.StringDecomposer
import net.minecraft.util.profiling.ProfilerFiller
import java.nio.file.Path
import java.util.*

object ServerLanguageManager {
    const val DEFAULT_LANGUAGE = "en_us"

    private val JSON = Json
    private var languageInfos = mapOf<String, LanguageInfo>()
    private var languages = mapOf<String, Language>()

    fun get(language: String): Language {
        return languages[language.lowercase()] ?: run {
            if (languages.isEmpty()) {
                throw IllegalStateException("Server languages not loaded yet!")
            } else {
                throw IllegalArgumentException("Unknown server language $language")
            }
        }
    }

    class ReloadListener : KotlinJsonResourceReloadListener(JSON, Constants.DATA_LANGUAGES_DIR) {
        override fun apply(
            elements: Map<Identifier, JsonElement>,
            resourceManager: ResourceManager,
            profiler: ProfilerFiller
        ) {
            languageInfos = resourceManager.getResource(resLoc("languages.json"))
                .orElseThrow()
                .openAsReader()
                .readText()
                .let { JSON.decodeFromString(MapSerializer(String.serializer(), LanguageInfo.serializer()), it) }
                .mapKeys { it.key.lowercase() }

            val loadedLanguages = elements.entries
                .groupBy { (key, _) -> Path.of(key.path).getName(0).toString().lowercase().also { langKey ->
                    if (!languageInfos.containsKey(langKey)) {
                        throw Exception("Tried specifying unknown language $langKey in server lang strings! Full path is $key")
                    }
                } }
                .mapValues { (_, entries) ->
                    entries.flatMap { (key, jsonValue) ->
                        val prefix = key.path.split("/").let{it.subList(1,it.size)}.joinToString(".")
                        processLangObject(if (prefix.isEmpty()) prefix else "$prefix.", jsonValue)
                    }.associate { it }
                }

            val languagesWithEmptyDefaults = languageInfos.keys.associateWith {
                loadedLanguages[it] ?: mapOf()
            }

            languages = languagesWithEmptyDefaults.mapValues { (langKey, strings) ->
                val languageInfo = languageInfos[langKey]!!

                if (langKey != DEFAULT_LANGUAGE) {
                    val defaultStrings = languagesWithEmptyDefaults[DEFAULT_LANGUAGE]!!
                    ServerLanguage(
                        (strings.keys + defaultStrings.keys).associateWith { strings[it] ?: defaultStrings[it]!! },
                        languageInfo.bidirectional
                    )
                } else {
                    ServerLanguage(strings, languageInfo.bidirectional)
                }
            }
        }

        private fun processLangObject(prefix: String, jsonValue: JsonElement): Iterable<Pair<String, String>> {
            return when (jsonValue) {
                is JsonPrimitive -> listOf(Pair(prefix.replace(Regex("\\.$"), ""), jsonValue.jsonPrimitive.content))
                is JsonObject -> jsonValue.entries.flatMap { (itemKey, item) ->
                    processLangObject("$prefix$itemKey.", item)
                }
                is JsonArray -> jsonValue.withIndex().flatMap { (itemIndex, item) ->
                    processLangObject("$prefix$itemIndex.", item)
                }
            }
        }
    }
}

class ServerLanguage (private val values: Map<String, String>, private val defaultRightToLeft: Boolean) : Language() {
    override fun getOrDefault(key: String, defaultValue: String): String = values.getOrDefault(key, defaultValue)

    override fun has(id: String): Boolean = values.containsKey(id)

    override fun isDefaultRightToLeft(): Boolean = defaultRightToLeft

    override fun getVisualOrder(text: FormattedText): FormattedCharSequence {
        // based on vanilla code
        return FormattedCharSequence { formattedCharSink: FormattedCharSink ->
            text.visit(
                { style: Style, string: String ->
                    if (StringDecomposer.iterateFormatted(string, style, formattedCharSink))
                        Optional.empty()
                    else
                        FormattedText.STOP_ITERATION
                },
                Style.EMPTY,
            ).isPresent
        }
    }
}