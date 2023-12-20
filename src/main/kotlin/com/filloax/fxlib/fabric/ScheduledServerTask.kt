package com.filloax.fxlib.fabric

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

import net.minecraft.server.MinecraftServer


class ScheduledServerTask(private val delayTicks: Int, private val task: Runnable, private val initialTick: Int) {
    companion object {
        private val tasks = mutableListOf<ScheduledServerTask>()

        fun schedule(server: MinecraftServer, delayTicks: Int, task: Runnable) {
            tasks.add(ScheduledServerTask(delayTicks, task, server.tickCount))
        }

        fun init() {
            ServerTickEvents.START_SERVER_TICK.register(this::onTick)
        }

        private fun onTick(server: MinecraftServer) {
            tasks.removeIf { task -> task.executeIfElapsed(server) }
        }
    }

    private fun executeIfElapsed(server: MinecraftServer): Boolean {
        val elapsedTicks: Int = server.tickCount - initialTick
        if (elapsedTicks >= delayTicks) {
            task.run()
            return true
        }
        return false
    }
}