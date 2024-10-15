package com.filloax.fxlib.platform

import com.filloax.fxlib.api.neoforge.EventOnce
import com.filloax.fxlib.api.platform.PlatformAbstractions
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks
import java.util.*

class PlatformAbstractionsNeo : PlatformAbstractions {
    override fun getServer(): MinecraftServer? {
        return ServerLifecycleHooks.getCurrentServer()
    }

    override fun isDevEnvironment(): Boolean {
        return !FMLLoader.isProduction()
    }

    override fun runOnEntityWhenPossible(level: ServerLevel, entityUUID: UUID, action: (Entity) -> Unit) {
        EventOnce.runOnEntityWhenPossible(level, entityUUID, action)
    }

    override fun runWhenServerStarted(
        server: MinecraftServer,
        onServerThread: Boolean,
        action: (MinecraftServer) -> Unit
    ) {
        EventOnce.runWhenServerStarted(server, onServerThread, action)
    }

    override fun runAtServerTickEnd(action: (MinecraftServer) -> Unit) {
        EventOnce.runEventOnce { event: ServerTickEvent.Post -> action(event.server) }
    }

    override fun runAtNextServerTickStart(action: (MinecraftServer) -> Unit) {
        EventOnce.runEventOnce { event: ServerTickEvent.Pre -> action(event.server) }
    }

    override fun runWhenChunkLoaded(level: ServerLevel, chunkPos: ChunkPos, action: (ServerLevel) -> Unit) {
        EventOnce.runWhenChunkLoaded(level, chunkPos, action)
    }

    override fun runWhenChunksLoaded(
        level: ServerLevel,
        minChunkPos: ChunkPos,
        maxChunkPos: ChunkPos,
        action: (ServerLevel) -> Unit
    ) {
        EventOnce.runWhenChunksLoaded(level, minChunkPos, maxChunkPos, action)
    }
}