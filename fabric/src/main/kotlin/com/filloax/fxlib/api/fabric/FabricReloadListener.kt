package com.filloax.fxlib.api.fabric

import net.fabricmc.fabric.impl.resource.FabricResourceReloader
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

// original source: https://github.com/TelepathicGrunt/RepurposedStructures
class FabricReloadListener(private val id: Identifier, private val listener: PreparableReloadListener): FabricResourceReloader {

    override fun `fabric$getId`(): Identifier {
        return id
    }

    override fun reload(
        sharedState: PreparableReloadListener.SharedState,
        exectutor: Executor,
        barrier: PreparationBarrier,
        applyExectutor: Executor
    ): CompletableFuture<Void> {
        return listener.reload(sharedState, exectutor, barrier, applyExectutor)
    }
}