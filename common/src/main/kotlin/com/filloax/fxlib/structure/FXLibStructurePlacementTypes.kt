package com.filloax.fxlib.structure

import com.filloax.fxlib.InternalUtils.resLoc
import com.filloax.fxlib.api.structure.tracking.FixedStructurePlacement
import net.minecraft.resources.Identifier
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType

object FXLibStructurePlacementTypes {
    val all = mutableMapOf<Identifier, StructurePlacementType<*>>()

    var FIXED = make("fixed") { FixedStructurePlacement.CODEC }

    private fun <SP : StructurePlacement> make(name: String, sp: StructurePlacementType<SP>): StructurePlacementType<SP> {
        all[resLoc(name)] = sp
        return sp
    }

    fun registerStructurePlacementTypes(registrator: (Identifier, StructurePlacementType<*>) -> Unit) {
        all.forEach { registrator(it.key, it.value) }
    }
}