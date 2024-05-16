package com.filloax.fxlib.api.codec

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.UnboundedMapCodec
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TestSerializerCodecs {
    @Serializable
    data class SampleDataClass(
        val a: Int,
        val b: String? = null,
        val c: SampleDataInnerClass? = null,
    )

    @Serializable
    data class SampleDataInnerClass(
        val foo: String? = null,
        val bar: Boolean = false
    )

    @Test
    fun testSerialize() {
        val obj = SampleDataClass(10, "TestFoo", null)
        val codec = SampleDataClass.serializer().codec()

        val jsonElement = codec.encodeJson(obj).getOrThrow { Exception("Couldn't encode!") }
        val referenceJsonElement = JsonObject()
        referenceJsonElement.add("b", JsonPrimitive("TestFoo"))
        referenceJsonElement.add("a", JsonPrimitive(10))
        assertEquals(jsonElement, referenceJsonElement)
    }

    @Test
    fun testDeserialize() {
        val codec = SampleDataClass.serializer().codec()

        val jsonElementCorrect = JsonObject()
        jsonElementCorrect.add("a", JsonPrimitive(20))
        jsonElementCorrect.add("b", JsonPrimitive("TestBar"))
        val parsedJsonElement = codec.decodeJson(jsonElementCorrect).getOrThrow { Exception("Couldn't decode!") }.first

        assertEquals(parsedJsonElement, SampleDataClass(20, "TestBar"))

        val jsonElementWrong = JsonObject()
        jsonElementWrong.add("a", JsonPrimitive("Ciao"))
        jsonElementWrong.add("c", JsonPrimitive(10))
        val parsedJsonWrong = codec.decodeJson(jsonElementWrong)

        assertTrue(parsedJsonWrong.isError)
    }

    @Test
    fun testSerializeInner() {
        val codec = SampleDataClass.serializer().codec()

        val obj = SampleDataClass(30, "TestInner", SampleDataInnerClass(
            foo = "Maremma"
        ))

        val jsonElement = codec.encodeJson(obj).getOrThrow { Exception("Couldn't encode!") }
        val referenceJsonElement = JsonObject()
        referenceJsonElement.add("b", JsonPrimitive("TestInner"))
        referenceJsonElement.add("a", JsonPrimitive(30))
        referenceJsonElement.add("c", JsonObject().apply {
            add("foo", JsonPrimitive("Maremma"))
        })
        assertEquals(jsonElement, referenceJsonElement)
    }

    @Test
    fun testSerializeList() {
        val codec = SampleDataClass.serializer().codec().listOf()

        val sampleList = listOf(
            SampleDataClass(1, "AMO"),
            SampleDataClass(2, "GUS", SampleDataInnerClass("hi", true)),
        )
        val jsonElement = codec.encodeJson(sampleList).getOrThrow { Exception("Couldn't encode!") }
        val referenceJsonElement = JsonArray().apply {
            add(JsonObject().apply {
                add("a", JsonPrimitive(1))
                add("b", JsonPrimitive("AMO"))
            })
            add(JsonObject().apply {
                add("a", JsonPrimitive(2))
                add("b", JsonPrimitive("GUS"))
                add("c", JsonObject().apply {
                    add("foo", JsonPrimitive("hi"))
                    add("bar", JsonPrimitive(true))
                })
            })
        }

        assertEquals(jsonElement, referenceJsonElement)
    }

    @Test
    fun testDeserializeList() {
        val codec = SampleDataClass.serializer().codec().listOf()

        val jsonElement = JsonArray().apply {
            add(JsonObject().apply {
                add("a", JsonPrimitive(1))
                add("b", JsonPrimitive("AMO"))
            })
            add(JsonObject().apply {
                add("a", JsonPrimitive(2))
                add("b", JsonPrimitive("GUS"))
                add("c", JsonObject().apply {
                    add("foo", JsonPrimitive("hi"))
                    add("bar", JsonPrimitive(true))
                })
            })
        }
        val list = codec.decodeJson(jsonElement).getOrThrow { Exception("Couldn't decode!") }.first

        assertEquals(list, listOf(
            SampleDataClass(1, "AMO"),
            SampleDataClass(2, "GUS", SampleDataInnerClass("hi", true)),
        ))
    }

    @Test
    fun testSerializeMap() {
        val codec = UnboundedMapCodec(Codec.STRING, SampleDataClass.serializer().codec())

        val sampleMap = mapOf(
            "first" to SampleDataClass(1, "AMO"),
            "second" to SampleDataClass(2, "GUS", SampleDataInnerClass("hi", true)),
        )
        val jsonElement = codec.encodeJson(sampleMap).getOrThrow { Exception("Couldn't encode!") }
        val referenceJsonElement = JsonObject().apply {
            add("first", JsonObject().apply {
                add("a", JsonPrimitive(1))
                add("b", JsonPrimitive("AMO"))
            })
            add("second", JsonObject().apply {
                add("a", JsonPrimitive(2))
                add("b", JsonPrimitive("GUS"))
                add("c", JsonObject().apply {
                    add("foo", JsonPrimitive("hi"))
                    add("bar", JsonPrimitive(true))
                })
            })
        }

        assertEquals(jsonElement, referenceJsonElement)
    }

    @Test
    fun testDeserializeMap() {
        val codec = UnboundedMapCodec(Codec.STRING, SampleDataClass.serializer().codec())

        val jsonElement = JsonObject().apply {
            add("first", JsonObject().apply {
                add("a", JsonPrimitive(1))
                add("b", JsonPrimitive("AMO"))
            })
            add("second", JsonObject().apply {
                add("a", JsonPrimitive(2))
                add("b", JsonPrimitive("GUS"))
                add("c", JsonObject().apply {
                    add("foo", JsonPrimitive("hi"))
                    add("bar", JsonPrimitive(true))
                })
            })
        }
        val list = codec.decodeJson(jsonElement).getOrThrow { Exception("Couldn't decode!") }.first

        assertEquals(list, mapOf(
            "first" to SampleDataClass(1, "AMO"),
            "second" to SampleDataClass(2, "GUS", SampleDataInnerClass("hi", true)),
        ))
    }

    @Test
    fun testDeserializeDefaults() {
        val codec = SampleDataClass.serializer().codec()

        val jsonElementCorrect = JsonObject()
        jsonElementCorrect.add("a", JsonPrimitive(20))
        jsonElementCorrect.add("b", JsonPrimitive("TestBar"))
        jsonElementCorrect.add("c", JsonObject().apply {
            add("foo", JsonPrimitive("test"))
        })
        val parsedJsonElement = codec.decodeJson(jsonElementCorrect).getOrThrow { Exception("Couldn't decode!") }.first

        assertEquals(parsedJsonElement, SampleDataClass(20, "TestBar", SampleDataInnerClass("test", false)))
    }
}