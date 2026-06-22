package com.filloax.fxlib.gametest

import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class FxLibFabricGameTests {
    @GameTest
    fun testMixinsLoaded(helper: GameTestHelper) {
        FxLibGameTests.testMixinsLoaded(helper)
    }
}
