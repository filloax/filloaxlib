package com.filloax.fxlib

import com.filloax.fxlib.api.fabric.EventOnce
import com.filloax.fxlib.platform.fxLibEvents
import com.filloax.fxlib.structure.FXLibStructurePlacementTypes
import com.filloax.fxlib.structure.FXLibStructures
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

object FxLibFabric : ModInitializer, VersionFxLib() {
    /**
     * Runs the mod initializer.
     */
    override fun onInitialize() {
        initialize()
    }

    override fun initPlatformCallbacks() {
        fxLibEvents.onServerStopped { server ->
            EventOnce.Callbacks.onServerShutdown(server)
        }
    }

    override fun initRegistryStructurePlacementType() {
        FXLibStructurePlacementTypes.registerStructurePlacementTypes { id, value ->
            Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, id, value)
        }
    }

    override fun initRegistryStructureType() {
        FXLibStructures.registerStructureTypes { id, value ->
            Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id, value)
        }
    }
}