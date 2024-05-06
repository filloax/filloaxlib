package com.filloax.fxlib.structure

import com.filloax.fxlib.FXLibUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType

object FXLibStructures {
    @JvmStatic
    val allTypes = mutableMapOf<ResourceLocation, StructureType<*>>()

    val JIGSAW_FORCE_POS = makeType("jigsaw_forced_pos") { ForcePosJigsawStructure.CODEC.codec() }

    private fun <T : Structure> makeType(name: String, type: StructureType<T>): StructureType<T> {
        val id = FXLibUtils.resLoc(name)
        allTypes[id] = type
        return type
    }

    fun registerStructureTypes(registrator: (ResourceLocation, StructureType<*>) -> Unit) {
        allTypes.forEach{
            registrator(it.key, it.value)
        }
    }
}