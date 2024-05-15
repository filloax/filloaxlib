package com.filloax.fxlib.api.fabric

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

// source: https://github.com/TelepathicGrunt/RepurposedStructures
class FabricReloadListener(private val id: ResourceLocation, private val listener: PreparableReloadListener) :
    IdentifiableResourceReloadListener {

    override fun getFabricId(): ResourceLocation {
        return id
    }

    override fun reload(
        barrier: PreparationBarrier, manager: ResourceManager,
        profiler: ProfilerFiller, profiler2: ProfilerFiller, executor: Executor, executor2: Executor
    ): CompletableFuture<Void> {
        return listener.reload(barrier, manager, profiler, profiler2, executor, executor2)
    }
}