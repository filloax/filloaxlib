package com.filloax.fxlib.json

import com.google.gson.Gson
import com.google.gson.JsonElement as GsonElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
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

fun <T : Any> DataProvider.saveStable(writer: CachedOutput, serializer: KSerializer<T>, value: T, path: Path): CompletableFuture<*> {
    val jsonString = jsonSimple.encodeToString(serializer, value)
    val gsonValue = gson.fromJson(jsonString, GsonElement::class.java)
    return DataProvider.saveStable(writer, gsonValue, path)
}
