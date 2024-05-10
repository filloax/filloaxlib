package com.filloax.fxlib

import java.util.*
import kotlin.reflect.KProperty1

// Useful for defining codecs with nullable fields
fun <T, V> KProperty1<T, V?>.optional() = { receiver: T ->
    Optional.ofNullable(this.get(receiver))
}