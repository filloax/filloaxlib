package com.filloax.fxlib

import com.filloax.fxlib.platform.fxLibEvents
import com.filloax.fxlib.structure.FixedStructureGenerationImpl

abstract class VersionFxLib : FxLib() {
    override fun initCallbacks() {
        fxLibEvents.onServerStopped { server ->
            FixedStructureGenerationImpl.onServerStopped()
        }

        fxLibEvents.onStartServerTick { server ->
            ScheduledServerTask.onStartServerTick(server)
        }

        fxLibEvents.onLoadChunk { level, chunk ->
            FixedStructureGenerationImpl.onLoadChunk(level, chunk)
        }
    }
}