package com.filloax.fxlib.platform.fabric

import com.filloax.fxlib.FxLibEvents
import com.filloax.fxlib.platform.IPlatformEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

class FabricEvents : IPlatformEvents {
    override fun initEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(FxLibEvents::onServerStarting)
        ServerLifecycleEvents.SERVER_STARTED.register(FxLibEvents::onServerStarted)
        ServerLifecycleEvents.SERVER_STOPPING.register(FxLibEvents::onServerStopping)
        ServerLifecycleEvents.SERVER_STOPPED.register(FxLibEvents::onServerStopped)

        ServerChunkEvents.CHUNK_LOAD.register(FxLibEvents::onLoadChunk)
    }
}