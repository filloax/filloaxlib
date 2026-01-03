package com.filloax.fxlib.structure

import com.filloax.fxlib.InternalUtils
import com.filloax.fxlib.api.structure.ForcePosJigsawStructure
import net.minecraft.resources.Identifier
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType

object FXLibStructures {
    @JvmStatic
    val allTypes = mutableMapOf<Identifier, StructureType<*>>()

    val JIGSAW_FORCE_POS = makeType("jigsaw_forced_pos") { ForcePosJigsawStructure.CODEC }

    private fun <T : Structure> makeType(name: String, type: StructureType<T>): StructureType<T> {
        val id = InternalUtils.resLoc(name)
        allTypes[id] = type
        return type
    }

    fun registerStructureTypes(registrator: (Identifier, StructureType<*>) -> Unit) {
        allTypes.forEach{
            registrator(it.key, it.value)
        }
    }
}