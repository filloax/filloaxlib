package com.filloax.fxlib

import com.filloax.fxlib.structure.FixedStructureGenerationImpl
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.LevelChunk

object FxLibEvents {
    fun onServerStarting(server: MinecraftServer) {
    }

    fun onServerStarted(server: MinecraftServer) {
    }

    fun onServerStopping(server: MinecraftServer) {
    }

    fun onServerStopped(server: MinecraftServer) {
        FixedStructureGenerationImpl.onServerStopped()
    }

    fun onLoadChunk(level: ServerLevel, chunk: LevelChunk) {
        FixedStructureGenerationImpl.onLoadChunk(level, chunk)
    }
}