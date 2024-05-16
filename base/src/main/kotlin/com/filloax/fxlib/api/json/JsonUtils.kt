package com.filloax.fxlib.api.json

import com.google.gson.Gson
import com.google.gson.JsonElement as GsonElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}
private val jsonSimple = Json {
    ignoreUnknownKeys = true
}

private val gson = Gson()

fun GsonElement.toKotlin(): JsonElement {
    val jsonString = gson.toJson(this)
    return jsonSimple.parseToJsonElement(jsonString)
}
fun JsonElement.toGson(): GsonElement {
    val jsonString = jsonSimple.encodeToString(this)
    return gson.fromJson(jsonString, GsonElement::class.java)
}

fun <T : Any> DataProvider.saveStable(writer: CachedOutput, serializer: KSerializer<T>, value: T, path: Path): CompletableFuture<*> {
    val jsonString = jsonSimple.encodeToString(serializer, value)
    val gsonValue = gson.fromJson(jsonString, GsonElement::class.java)
    return DataProvider.saveStable(writer, gsonValue, path)
}
