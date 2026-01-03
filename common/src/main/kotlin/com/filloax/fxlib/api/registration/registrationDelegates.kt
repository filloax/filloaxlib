package com.filloax.fxlib.api.registration

import com.filloax.fxlib.FxLib
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import kotlin.reflect.KProperty

/**
 * Use if you want separation of concerns between static classes/objects containing all X (effects, etc) of your mod
 * for loader compatibility while also assigning their static variables a Holder instance like in vanilla.
 * Use with kotlin delegates to delay the actual setting of the holder in the delegate until after the registry is initialized,
 * which means you CANNOT use variables that delegate to this class before the registry is initialized. Usage example:
 *
 * ```kt
 * val MY_EFFECT by make("my_effect", MyEffect(MobEffectCategory.BENEFICIAL, 15630397))
 *
 * private fun make(name: String, effect: MobEffect) = RegistryHolderDelegate(resLoc(name), effect).apply {
 *     if (all.containsKey(id))
 *         throw IllegalArgumentException("Effect $name already registered!")
 *     all[id] = this
 * }
 *
 * fun registerEffects(registrator: (Identifier, MobEffect) -> Holder<MobEffect>) {
 *     all.values.forEach{
 *         it.initHolder(registrator(it.id, it.value))
 *     }
 * }
 * ```
 */
class RegistryHolderDelegate<T>(val id: Identifier, val value: T) {
    var holder: Holder<T>? = null

    fun initHolder(holder: Holder<T>) {
        this.holder = holder
    }

    operator fun getValue(owner: Any, property: KProperty<*>): Holder<T> {
        return holder ?: throw IllegalStateException("Not initialized holder yet for $id")
    }
}

/**
 * Simpler version of [RegistryHolderDelegate], for when you simply need a
 * direct value instead of a Holder but different loaders may init it later.
 * (For example: EntityTypes).
 *
 * Usage:
 * ```kt
 *    private fun <T : LivingEntity> make(
 *         name: String,
 *         entityTypeBuilder: EntityType.Builder<T>,
 *     ) = registryDelegate(Identifier.fromNamespaceAndPath("test", name)) {
 *         all[id] = {
 *             val entityType = entityTypeBuilder.build(id.toString())
 *             init(entityType)
 *
 *             entityType
 *         }
 *     }
 *
 *     fun registerEntityTypes(registrator: (Identifier, EntityType<*>) -> Unit) {
 *         all.forEach {
 *             registrator(it.key, it.value())
 *         }
 *     }
 * ```
 */
class RegistryDelegate<T>(val id: Identifier, val clazz: Class<T>) {
    var value: T? = null

    fun init(value: T) {
        this.value = value
        FxLib.logger.debug("Initialized registry delegate $id of class $clazz")
    }

    operator fun getValue(owner: Any, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Not initialized registry delegate value yet!")
    }
}

/**
 * See [RegistryDelegate]
 */
inline fun <reified T> registryDelegate(id: Identifier, block: RegistryDelegate<T>.() -> Unit) = RegistryDelegate(id, T::class.java).also(block)
/**
 * See [RegistryDelegate]
 */
inline fun <reified T> registryDelegate(id: Identifier) = RegistryDelegate(id, T::class.java)