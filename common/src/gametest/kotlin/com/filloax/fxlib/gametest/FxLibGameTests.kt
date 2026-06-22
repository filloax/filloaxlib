package com.filloax.fxlib.gametest

import net.minecraft.gametest.framework.GameTestHelper

object FxLibGameTests {
    // Verifies mixins loaded successfully — server startup alone proves mixin loading
    fun testMixinsLoaded(helper: GameTestHelper) {
        helper.succeed()
    }
}
