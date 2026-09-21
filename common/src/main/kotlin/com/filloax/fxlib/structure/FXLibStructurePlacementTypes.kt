package com.filloax.fxlib.structure

import com.filloax.fxlib.InternalUtils.resLoc
import com.filloax.fxlib.api.structure.tracking.FixedStructurePlacement
import com.mojang.serialization.MapCodec
import net.minecraft.resources.Identifier
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement

object FXLibStructurePlacementTypes {
    val all = mutableMapOf<Identifier, MapCodec<out StructurePlacement>>()

    var FIXED = make("fixed", FixedStructurePlacement.CODEC)

    private fun <SP : StructurePlacement> make(name: String, codec: MapCodec<SP>): MapCodec<SP> {
        all[resLoc(name)] = codec
        return codec
    }

    fun registerStructurePlacementTypes(registrator: (Identifier, MapCodec<out StructurePlacement>) -> Unit) {
        all.forEach { registrator(it.key, it.value) }
    }
}
