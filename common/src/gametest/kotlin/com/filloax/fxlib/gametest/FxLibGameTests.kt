package com.filloax.fxlib.gametest

import com.filloax.fxlib.api.entity.getPersistData
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.Entity

object FxLibGameTests {
    // Verifies mixins loaded successfully — server startup alone proves mixin loading
    fun testMixinsLoaded(helper: GameTestHelper) {
        helper.succeed()
    }

    // The player entity is recreated on respawn and end exit, persistent data must be copied over
    fun testPersistDataKeptOnRespawn(helper: GameTestHelper) {
        val playerList = helper.level.server.playerList
        @Suppress("DEPRECATION")
        val player = helper.makeMockServerPlayerInLevel()
        player.getPersistData().putString("fxlib_test", "kept")

        val respawned = playerList.respawn(player, false, Entity.RemovalReason.KILLED)
        helper.assertTrue(respawned !== player, "respawn should create a new player entity")
        helper.assertTrue(
            respawned.getPersistData().getString("fxlib_test").orElse(null) == "kept",
            "persistent data lost on death respawn",
        )

        val afterEnd = playerList.respawn(respawned, true, Entity.RemovalReason.CHANGED_DIMENSION)
        helper.assertTrue(
            afterEnd.getPersistData().getString("fxlib_test").orElse(null) == "kept",
            "persistent data lost on end exit",
        )
        helper.succeed()
    }
}
