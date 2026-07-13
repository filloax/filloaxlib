package com.filloax.fxlib.platform

import com.filloax.fxlib.api.platform.ServiceUtil
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.chunk.LevelChunk

typealias ServerEvent = (server: MinecraftServer) -> Unit

/**
 * Implement with the platform-specific events (or mixins if missing)
 * to register these events
 */
interface FxLibEvents {
    fun onServerStarting(event: ServerEvent)

    fun onServerStarted(event: ServerEvent)

    fun onServerStopping(event: ServerEvent)

    fun onServerStopped(event: ServerEvent)

    fun onLoadChunk(event: (level: ServerLevel, chunk: LevelChunk, generated: Boolean) -> Unit)

    fun onStartServerTick(event: ServerEvent)

    fun onStartTrackingEntity(event: (entity: Entity, player: ServerPlayer) -> Unit)
}

val fxLibEvents: FxLibEvents = ServiceUtil.findService(FxLibEvents::class.java)