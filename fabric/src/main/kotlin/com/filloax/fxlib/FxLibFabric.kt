package com.filloax.fxlib

import com.filloax.fxlib.InternalUtils.resLoc
import com.filloax.fxlib.api.fabric.EventOnce
import com.filloax.fxlib.api.fabric.FabricReloadListener
import com.filloax.fxlib.api.lang.server.ServerLanguageManager
import com.filloax.fxlib.platform.fxLibEvents
import com.filloax.fxlib.structure.FXLibStructurePlacementTypes
import com.filloax.fxlib.structure.FXLibStructurePoolElements
import com.filloax.fxlib.structure.FXLibStructures
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.PackType

object FxLibFabric : ModInitializer, VersionFxLib() {
    /**
     * Runs the mod initializer.
     */
    override fun onInitialize() {
        initialize()

        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(
            resLoc("fx_lib_reloader"),
            FabricReloadListener(
                resLoc(Constants.DATA_LANGUAGES_DIR),
                ServerLanguageManager.ReloadListener(),
            )
        )
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

    override fun initRegistryStructurePoolElementType() {
        FXLibStructurePoolElements.registerStructurePoolElementTypes { id, value ->
            Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, id, value)
        }
    }

    override fun initRegistryStructureType() {
        FXLibStructures.registerStructureTypes { id, value ->
            Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id, value)
        }
    }
}