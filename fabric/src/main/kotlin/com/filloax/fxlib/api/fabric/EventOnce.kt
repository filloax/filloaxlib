package com.filloax.fxlib.api.fabric

import com.filloax.fxlib.*
import com.filloax.fxlib.api.removeAllCountDuplicates
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.*

object EventOnce {
    // Do not use actual queue classes, due to implementation of iteration below
    private val eventQueues = mutableMapOf<Event<out Any>, MutableList<OneOffEvent<Any>>>()
    val registeredEvents = mutableSetOf<Event<out Any>>()

    object Callbacks {
        fun onServerShutdown(server: MinecraftServer) {
            eventQueues.forEach { (event, queue) ->
                synchronized(queue) {
                    queue.removeIf { it.clearOnServerShutdown }
                }
            }
        }
    }

    data class OneOffEvent<out T : Any>(
        val listener: T,
        val clearOnServerShutdown: Boolean = true,
        // Predicate: TODO, maybe later, maybe not
//        val runPredicate: MethodHandle? = null,
        val throwOnFail: Boolean = false,
    )

    fun <T: Any> addToQueue(event: Event<T>, item: OneOffEvent<T>) {
        val queue = eventQueues.computeIfAbsent(event) { mutableListOf() }
        synchronized(queue) {
            queue.add(item)
        }
    }

    /**
     * Run a Fabric event only once, as soon as possible after this method is called.
     * Works with methods without a return type.
     */
    inline fun <reified T : Any> runEventOnce(
        event: Event<T>, listener: T,
        clearOnServerShutdown: Boolean = true,
//        predicate: Function<Boolean>? = null,
        throwOnFail: Boolean = false,
    ) {
        // in won't work with generics
        if (!registeredEvents.any { it == event }) {
            // register proxy event and check if it's a proper functional interface
            val method = ReflectionEventsHelper.getFunctionalInterfaceMethod<T>()
            val handle = method.toHandle()
            event.register(Proxy.newProxyInstance(
                this::class.java.classLoader,
                arrayOf(T::class.java)
            ) { _, _, args ->
                handleQueue(event, handle, args)
            } as T)
            registeredEvents.add(event)
        }

//        val predicateHandler = predicate?.let{
//            val method = ReflectionEventsHelper.getFunctionalInterfaceMethod(it.javaClass)
//            method.toHandle()
//        }

        addToQueue(event, OneOffEvent(
            listener,
            clearOnServerShutdown,
            // predicateHandler,
            throwOnFail,
        )
        )
    }

    fun runOnEntityWhenPossible(level: ServerLevel, entityUUID: UUID, action: (Entity) -> Unit) {
        val entity = level.getEntity(entityUUID)
        if (entity != null) {
            action(entity)
        } else {
            runEventOnce(ServerEntityEvents.ENTITY_LOAD, object : ServerEntityEvents.Load {
                override fun onLoad(entity: Entity, world: ServerLevel) {
                    // If right entity, run, otherwise, reschedule
                    if (entity.uuid == entityUUID) {
                        action(entity)
                    } else {
                        runEventOnce(ServerEntityEvents.ENTITY_LOAD, this)
                    }
                }
            })
        }
    }

    fun runWhenServerStarted(server: MinecraftServer, action: (MinecraftServer) -> Unit) {
        runWhenServerStarted(server, false, action)
    }

    fun runWhenServerStarted(server: MinecraftServer, onServerThread: Boolean, action: (MinecraftServer) -> Unit) {
        val callback = if (onServerThread) {
            ServerLifecycleEvents.ServerStarted { srv -> srv.submit { action(srv) } }
        } else {
            ServerLifecycleEvents.ServerStarted { srv -> action(srv) }
        }
        if (server.isReady) {
            callback.onServerStarted(server)
        } else {
            runEventOnce(ServerLifecycleEvents.SERVER_STARTED, callback)
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
            runEventOnce(ServerChunkEvents.CHUNK_LOAD, object : ServerChunkEvents.Load {
                override fun onChunkLoad(level2: ServerLevel, chunk: LevelChunk) {
                    var reschedule = false
                    if (level2 != level) {
                        reschedule = true
                    } else {
                        val allLoaded2 = ChunkPos.rangeClosed(minChunkPos, maxChunkPos).allMatch {
                            level2.isLoaded(it.worldPosition)
                        }
                        if (allLoaded2) {
                            action(level2)
                        } else {
                            reschedule = true
                        }
                    }
                    if (reschedule) {
                        runEventOnce(ServerChunkEvents.CHUNK_LOAD, this)
                    }
                }
            })
        }
    }

    fun <T : Any> handleQueue(event: Event<T>, handle: MethodHandle, args: Array<Any>) {
        // Iterate on copy of queue, so if callbacks change it, it won't affect the iteration
        @SuppressWarnings
        val queue = (eventQueues[event] ?: run {
            FxLib.logger.error("No queue for event $event")
            return
        }) as MutableList<OneOffEvent<T>>
        val iterQueue = mutableListOf<OneOffEvent<T>>().also { it.addAll(queue) }
        for (handler in iterQueue) {
            try {
                val listener = handler.listener
                handle.invokeWithArguments(ReflectionEventsHelper.insertElementAtStart(args, listener))
            } catch (e: Exception) {
                if (handler.throwOnFail) {
                    throw Exception("throwOnFail one-off handler $handler failed:", e)
                } else {
                    FxLib.logger.error("Error in running one-off event $handler! \n${e.stackTraceToString()}")
                }
            }
        }
        if (iterQueue.isNotEmpty()) {
            synchronized(queue) {
                // Remove all, but keep duplicates in case something reschedules etc
                queue.removeAllCountDuplicates(iterQueue)
            }
        }
    }
}

/**
 * Thanks, ChatGPT, for this, heavily based on the fabric API events implementation
 * in `EventFactoryImpl`.
 * Purpose: to allow for making proxies/registering arbitrary events even in a typed
 * language like java/kotlin
 */
object ReflectionEventsHelper {
    inline fun <reified T> getFunctionalInterfaceMethod(): Method
        = getFunctionalInterfaceMethod(T::class.java)

    fun <T> getFunctionalInterfaceMethod(handlerClass: Class<T>): Method {
        return handlerClass.methods
            .filter { !it.isSynthetic }
            .singleOrNull { (it.modifiers and (Modifier.STRICT or Modifier.PRIVATE)) == 0 }
            ?: throw IllegalStateException("No or multiple virtual methods in $handlerClass; cannot identify functional interface method!")
    }

    inline fun <reified T> insertElementAtStart(array: Array<T>, element: T): List<T> {
        return listOf(element) + array
    }
}

/**
 * Converts a Java [Method] to a [MethodHandle].
 *
 * @return The [MethodHandle] for the [Method].
 * @throws RuntimeException if an error occurs during the conversion.
 */
fun Method.toHandle(): MethodHandle {
    return try {
        MethodHandles.lookup().unreflect(this)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    }
}
