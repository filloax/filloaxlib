package com.filloax.fxlib.api

import com.filloax.fxlib.api.platform.getPlatformAbstractions
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import java.util.*

object EventUtil {
    private val platformAbstractions = getPlatformAbstractions()

    /**
     * Run the action on the entity immediately if loaded, as soon as it's loaded
     * otherwise. Will not persist on game reload.
     */
    fun runOnEntityWhenPossible(level: ServerLevel, entityUUID: UUID, action: (Entity) -> Unit)
        = platformAbstractions.runOnEntityWhenPossible(level, entityUUID, action)

    /**
     * Run now if server started, or wait for server to start then run otherwise.
     */
    fun runWhenServerStarted(server: MinecraftServer, action: (MinecraftServer) -> Unit)
        = runWhenServerStarted(server, false, action)

    fun runAtServerTickEnd(action: (MinecraftServer) -> Unit)
        = platformAbstractions.runAtServerTickEnd(action)
    fun runAtNextServerTickStart(action: (MinecraftServer) -> Unit)
        = platformAbstractions.runAtNextServerTickStart(action)

    /**
     * Run now if server started, or wait for server to start then run otherwise.
     * @param onServerThread If set, run on server thread, in case you want to be
     *  safe around multithreaded messing.
     */
    fun runWhenServerStarted(server: MinecraftServer, onServerThread: Boolean, action: (MinecraftServer) -> Unit)
        = platformAbstractions.runWhenServerStarted(server, onServerThread, action)
    fun runWhenChunkLoaded(level: ServerLevel, chunkPos: ChunkPos, action: (ServerLevel) -> Unit)
        = platformAbstractions.runWhenChunkLoaded(level, chunkPos, action)

    /**
     * Execute code when all the chunks in the surrounding area are loaded, or immediately if loaded already.
     * Note that this isn't assured to ever run depending on area, as if big enough chunks on one end might be
     * unloaded when the other end is loaded; use forced chunks for this, in case.
     */
    fun runWhenChunksLoaded(level: ServerLevel, minChunkPos: ChunkPos, maxChunkPos: ChunkPos, action: (ServerLevel) -> Unit)
        = platformAbstractions.runWhenChunksLoaded(level, minChunkPos, maxChunkPos, action)
}