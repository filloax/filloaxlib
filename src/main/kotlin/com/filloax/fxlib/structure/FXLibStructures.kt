package com.filloax.fxlib.structure

import com.filloax.fxlib.FXLibUtils
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType

object FXLibStructures {
    @JvmStatic
    val allTypes = mutableMapOf<ResourceLocation, StructureType<*>>()

    val JIGSAW_FORCE_POS = registerType("jigsaw_forced_pos") { ForcePosJigsawStructure.CODEC }

    private fun <T : Structure> registerType(name: String, type: StructureType<T>): StructureType<T> {
        val id = FXLibUtils.resLoc(name)
        allTypes[id] = type
        return type
    }

    fun init(registry: Registry<StructureType<*>>) {
        allTypes.forEach{
            Registry.register(registry, it.key, it.value)
        }
    }
}