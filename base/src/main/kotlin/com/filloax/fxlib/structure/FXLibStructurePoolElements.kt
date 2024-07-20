package com.filloax.fxlib.structure

import com.filloax.fxlib.InternalUtils
import com.mojang.serialization.MapCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType

object FXLibStructurePoolElements {
    val all = mutableMapOf<ResourceLocation, StructurePoolElementType<*>>()

    private fun <T : StructurePoolElement> make(name: String, codec: MapCodec<T>): StructurePoolElementType<T> {
       val type = StructurePoolElementType { codec }
        all[InternalUtils.resLoc(name)] = type
        return type
    }

    fun registerStructurePoolElementTypes(registrator: (ResourceLocation, StructurePoolElementType<*>) -> Unit) {
        all.forEach { registrator(it.key, it.value) }
    }
}