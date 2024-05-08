package com.filloax.fxlib.registration

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KProperty

/**
 * Use if you want separation of concerns between static classes/objects containing all X (effects, etc) of your mod
 * for loader compatibility while also assigning their static variables a Holder instance like in vanilla.
 * Use with kotlin delegates to delay the actual setting of the holder in the delegate until after the registry is initialized,
 * which means you CANNOT use variables that delegate to this class before the registry is initialized. Usage example:
 *
 * ```kt
 * val MY_EFFECT = by make("my_effect", MyEffect(MobEffectCategory.BENEFICIAL, 15630397))
 *
 * private fun make(name: String, effect: MobEffect): RegistryHolderDelegate<MobEffect> {
 *   val key = ResourceLocation(MOD_ID, name)
 *   return RegistryHolderDelegate<MobEffect>(key, effect).also {
 *     all.add(it)
 *   }
 * }
 *
 * fun registerEffects(registrator: (ResourceLocation, MobEffect) -> Holder<MobEffect>) {
 *   all.forEach { it.initHolder(registrator(it.id, it.value)) }
 * }
 * ```
 */
class RegistryHolderDelegate<T>(val id: ResourceLocation, val value: T) {
    var holder: Holder<T>? = null

    fun initHolder(holder: Holder<T>) {
        this.holder = holder
    }

    operator fun getValue(owner: Any, property: KProperty<*>): Holder<T> {
        return holder ?: throw IllegalStateException("Not initialized holder yet for $id")
    }
}