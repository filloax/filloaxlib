package com.filloax.fxlib.api.registration

import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.commands.synchronization.ArgumentTypeInfos
import net.minecraft.core.Registry

object CommandsRegistrationHelper {
    // Adapted to kotlin classes as kotlin generics can get messy with this complexity
    fun <A : ArgumentType<*>, T : ArgumentTypeInfo.Template<A>> registerArgumentType(
        registry: Registry<ArgumentTypeInfo<*, *>>,
        id: String,
        argumentClass: Class<out A>,
        info: ArgumentTypeInfo<A, T>,
    ): ArgumentTypeInfo<A, T> {
        ArgumentTypeInfos.BY_CLASS[argumentClass] = info
        return Registry.register(registry, id, info)
    }
}