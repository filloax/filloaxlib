package com.filloax.fxlib.api.neoforge

import com.filloax.fxlib.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.neoforged.bus.api.Event
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import java.util.*
import java.util.function.Consumer

object EventOnce {
    // Do not use actual queue classes, due to implementation of iteration below
    val clearOnShutdown = mutableListOf<Consumer<out Event>>()

    object Callbacks {
        fun onServerShutdown(server: MinecraftServer) {
            clearOnShutdown.forEach(NeoForge.EVENT_BUS::unregister)
            clearOnShutdown.clear()
        }
    }

    data class OneOffEvent<out T : Any>(
        val listener: T,
        val clearOnServerShutdown: Boolean = true,
        // Predicate: TODO, maybe later, maybe not
//        val runPredicate: MethodHandle? = null,
        val throwOnFail: Boolean = false,
    )

    /**
     * Run a Neoforge event only once, as soon as possible after this method is called.
     * Works with methods without a return type.
     */
    inline fun <reified T : Event> runEventOnce(
        clearOnServerShutdown: Boolean = true,
        listener: Consumer<T>,
    ) {
        var autoRemovingListener: Consumer<T>? = null
        autoRemovingListener = Consumer { x ->
            listener.accept(x)
            NeoForge.EVENT_BUS.unregister(autoRemovingListener)
        }

        NeoForge.EVENT_BUS.addListener(autoRemovingListener)

        if (clearOnServerShutdown) {
            clearOnShutdown.add(autoRemovingListener)
        }
    }

    inline fun <reified T : Event> runEventOnce(listener: Consumer<T>) = runEventOnce(true, listener)

    fun runOnEntityWhenPossible(level: ServerLevel, entityUUID: UUID, action: (Entity) -> Unit) {
        val entity = level.getEntity(entityUUID)
        if (entity != null) {
            action(entity)
        } else {
            runEventOnce { event: EntityJoinLevelEvent ->
                // If right entity, run, otherwise, reschedule
                if (event.entity.uuid == entityUUID) {
                    action(event.entity)
                } else {
                    runOnEntityWhenPossible(level, entityUUID, action)
                }
            }
        }
    }

    fun runWhenServerStarted(server: MinecraftServer, action: (MinecraftServer) -> Unit) {
        runWhenServerStarted(server, false, action)
    }

    fun runWhenServerStarted(server: MinecraftServer, onServerThread: Boolean, action: (MinecraftServer) -> Unit) {
        val callback = if (onServerThread) {
            { srv: MinecraftServer -> srv.submit { action(srv) } }
        } else {
            { srv: MinecraftServer -> action(srv) }
        }
        if (server.isReady) {
            callback(server)
        } else {
            runEventOnce { event: ServerStartedEvent -> callback(event.server) }
        }
    }

    fun runWhenChunkLoaded(level: ServerLevel, chunkPos: ChunkPos, action: (ServerLevel) -> Unit) {
        runWhenChunksLoaded(level, chunkPos, chunkPos, action)
    }

    fun runWhenChunksLoaded(level: ServerLevel, minChunkPos: ChunkPos, maxChunkPos: ChunkPos, action: (ServerLevel) -> Unit) {
        val allLoaded = ChunkPos.rangeClosed(minChunkPos, maxChunkPos).allMatch {
            level.isLoaded(it.worldPosition)
        }
        if (allLoaded) {
            action(level)
        } else {
            lateinit var listener: Consumer<ChunkEvent.Load>
            listener = Consumer { event: ChunkEvent.Load ->
                var reschedule = false
                if (event.level != level) {
                    reschedule = true
                } else {
                    val allLoaded2 = ChunkPos.rangeClosed(minChunkPos, maxChunkPos).allMatch {
                        level.isLoaded(it.worldPosition)
                    }
                    if (allLoaded2) {
                        action(level)
                    } else {
                        reschedule = true
                    }
                }
                if (reschedule) {
                    runEventOnce(listener)
                }
            }
            runEventOnce(listener)
        }
    }
}
