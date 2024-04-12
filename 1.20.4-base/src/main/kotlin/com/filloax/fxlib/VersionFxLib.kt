package com.filloax.fxlib

import com.filloax.fxlib.platform.fxLibEvents
import com.filloax.fxlib.platform.getPlatformAbstractions
import com.filloax.fxlib.structure.FixedStructureGenerationImpl

abstract class VersionFxLib : FxLib() {
    final override fun initCallbacks() {
        fxLibEvents.onServerStopped { server ->
            FixedStructureGenerationImpl.onServerStopped()
        }

        fxLibEvents.onStartServerTick { server ->
            ScheduledServerTask.onStartServerTick(server)
        }

        fxLibEvents.onLoadChunk { level, chunk ->
            FixedStructureGenerationImpl.onLoadChunk(level, chunk)
        }

        initPlatformCallbacks()
    }

    // For Platform implementation-specific callbacks
    open fun initPlatformCallbacks() {}

    final override fun initRegistries() {
        initRegistryStructureType()
        initRegistryStructurePlacementType()
    }

    abstract fun initRegistryStructureType()
    abstract fun initRegistryStructurePlacementType()
}