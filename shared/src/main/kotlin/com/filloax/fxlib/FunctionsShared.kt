package com.filloax.fxlib

import java.util.function.Predicate

val ALWAYS_TRUE: Predicate<Any> = Predicate<Any> { true }

fun <T> alwaysTruePredicate(): (T) -> Boolean = { true }