package com.filloax.fxlib.api

import com.filloax.fxlib.api.platform.getPlatformAbstractions
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import java.util.*
import java.util.function.Consumer

/**
 * Useful for cross-platform custom events (use the exposed kotlin-events library for this)
 */
enum class TriState {
    TRUE,
    FALSE,
    DEFAULT,
    ;

    /**
     * Will throw InvalidStateException if DEFAULT
     */
    fun toBool(): Boolean = toBoolNullable() ?: throw IllegalStateException("TriState must not be default to convert to boolean!")

    fun toBoolNullable(): Boolean? = when(this) {
        TRUE -> true
        FALSE -> false
        DEFAULT -> null
    }

    fun ifPresent(action: (Boolean) -> Unit) {
        toBoolNullable()?.let { action(it) }
    }
    fun ifPresent(action: Consumer<Boolean>) {
        toBoolNullable()?.let { action.accept(it) }
    }
}

/**
 * Useful for cross-platform custom events (use the exposed kotlin-events library for this)
 */
abstract class EventWithResult<T> {
    var result: T? = null;

    fun setResultIfBlank(value: T) {
        if (result == null) {
            result = value
        }
    }
}

abstract class EventWithTristate {
    var result: TriState = TriState.DEFAULT

    fun setResultIfBlank(value: TriState) {
        if (result == TriState.DEFAULT) {
            result = value
        }
    }
}

/**
 * Assorted event-related utility functions, mainly used to run a specific modloader event once or ASAP
 */
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