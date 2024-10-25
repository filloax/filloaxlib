package com.filloax.fxlib.utils

import org.jetbrains.annotations.ApiStatus

// Same as kotlin ones, but will return null if no value in also the default lambda

@ApiStatus.Internal
interface MapWithNullableDefault<K, out V> : Map<K, V> {
    val map: Map<K, V>
    fun getOrImplicitDefault(key: K): V?
}

@ApiStatus.Internal
interface MutableMapWithNullableDefault<K, V> : MutableMap<K, V>, MapWithNullableDefault<K, V> {
    override val map: MutableMap<K, V>
}

@ApiStatus.Internal
class MapWithNullableDefaultImpl<K, out V>(public override val map: Map<K, V>, private val default: (key: K) -> V?) : MapWithNullableDefault<K, V> {
    override fun equals(other: Any?): Boolean = map.equals(other)
    override fun hashCode(): Int = map.hashCode()
    override fun toString(): String = map.toString()
    override val size: Int get() = map.size
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(value: @UnsafeVariance V): Boolean = map.containsValue(value)
    override fun get(key: K): V? = map.get(key)
    override val keys: Set<K> get() = map.keys
    override val values: Collection<V> get() = map.values
    override val entries: Set<Map.Entry<K, V>> get() = map.entries

    override fun getOrImplicitDefault(key: K): V? = map[key] ?: default(key)
}

@ApiStatus.Internal
class MutableMapWithNullableDefaultImpl<K, V>(public override val map: MutableMap<K, V>, private val default: (key: K) -> V?) : MutableMapWithNullableDefault<K, V> {
    override fun equals(other: Any?): Boolean = map.equals(other)
    override fun hashCode(): Int = map.hashCode()
    override fun toString(): String = map.toString()
    override val size: Int get() = map.size
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(value: @UnsafeVariance V): Boolean = map.containsValue(value)
    override fun get(key: K): V? = map.get(key)
    override val keys: MutableSet<K> get() = map.keys
    override val values: MutableCollection<V> get() = map.values
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries

    override fun put(key: K, value: V): V? = map.put(key, value)
    override fun remove(key: K): V? = map.remove(key)
    override fun putAll(from: Map<out K, V>) = map.putAll(from)
    override fun clear() = map.clear()

    override fun getOrImplicitDefault(key: K): V? = map[key] ?: default(key)
}