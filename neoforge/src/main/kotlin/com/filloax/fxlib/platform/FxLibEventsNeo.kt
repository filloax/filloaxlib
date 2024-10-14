package com.filloax.fxlib.platform

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

class FxLibEventsNeo : FxLibEvents {
    override fun onServerStarting(event: ServerEvent) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ServerStartingEvent ->
            event(neoEvent.server)
        }
    }

    override fun onServerStarted(event: ServerEvent) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ServerStartedEvent ->
            event(neoEvent.server)
        }
    }

    override fun onServerStopping(event: ServerEvent) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ServerStoppingEvent ->
            event(neoEvent.server)
        }
    }

    override fun onServerStopped(event: ServerEvent) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ServerStoppedEvent ->
            event(neoEvent.server)
        }
    }

    override fun onLoadChunk(event: (level: ServerLevel, chunk: LevelChunk) -> Unit) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ChunkEvent.Load ->
            if (neoEvent.level is ServerLevel && neoEvent.chunk is LevelChunk) {
                event(neoEvent.level as ServerLevel, neoEvent.chunk as LevelChunk)
            }
        }
    }

    override fun onStartServerTick(event: ServerEvent) {
        NeoForge.EVENT_BUS.addListener { neoEvent: ServerTickEvent.Pre ->
            event(neoEvent.server)
        }
    }
}