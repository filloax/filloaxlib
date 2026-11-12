package com.filloax.fxlib.api

import net.minecraft.server.MinecraftServer


class ScheduledServerTask(private val delayTicks: Int, private val task: Runnable, private val initialTick: Int) {
    companion object {
        private val tasks = mutableListOf<ScheduledServerTask>()

        fun schedule(server: MinecraftServer, delayTicks: Int, task: Runnable) {
            synchronized(tasks) {
                tasks.add(ScheduledServerTask(delayTicks, task, server.tickCount))
            }
        }

        fun onStartServerTick(server: MinecraftServer) {
            val due = synchronized(tasks) {
                val elapsed = tasks.filter { it.isElapsed(server) }
                tasks.removeAll(elapsed.toSet())
                elapsed
            }
            due.forEach { it.task.run() }
        }
    }

    private fun isElapsed(server: MinecraftServer): Boolean {
        return server.tickCount - initialTick >= delayTicks
    }
}
