package com.filloax.fxlib.structure

import com.filloax.fxlib.FXLibUtils.resLoc
import com.filloax.fxlib.structure.tracking.FixedStructurePlacement
import com.mojang.serialization.Codec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType

object FXLibStructurePlacementTypes {
    val all = mutableMapOf<ResourceLocation, StructurePlacementType<*>>()

    var FIXED = make("fixed", FixedStructurePlacement.CODEC.codec())

    private fun <SP : StructurePlacement?> make(name: String, codec: Codec<SP>): StructurePlacementType<SP> {
        val st = StructurePlacementType { codec }
        all[resLoc(name)] = st
        return st
    }

    fun registerStructurePlacementTypes(registrator: (ResourceLocation, StructurePlacementType<*>) -> Unit) {
        all.forEach { registrator(it.key, it.value) }
    }
}