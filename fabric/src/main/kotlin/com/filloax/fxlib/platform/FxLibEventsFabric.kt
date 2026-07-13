package com.filloax.fxlib.platform

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.chunk.LevelChunk

class FxLibEventsFabric : FxLibEvents {
    override fun onServerStarting(event: ServerEvent) {
        ServerLifecycleEvents.SERVER_STARTING.register(event)
    }

    override fun onServerStarted(event: ServerEvent) {
        ServerLifecycleEvents.SERVER_STARTED.register(event)
    }

    override fun onServerStopping(event: ServerEvent) {
        ServerLifecycleEvents.SERVER_STOPPING.register(event)
    }

    override fun onServerStopped(event: ServerEvent) {
        ServerLifecycleEvents.SERVER_STOPPED.register(event)
    }

    override fun onLoadChunk(event: (level: ServerLevel, chunk: LevelChunk, generated: Boolean) -> Unit) {
        ServerChunkEvents.CHUNK_LOAD.register(event)
    }

    override fun onStartServerTick(event: ServerEvent) {
        ServerTickEvents.START_SERVER_TICK.register(event)
    }

    override fun onStartTrackingEntity(event: (entity: Entity, player: ServerPlayer) -> Unit) {
        EntityTrackingEvents.START_TRACKING.register(event)
    }
}