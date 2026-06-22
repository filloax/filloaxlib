package com.filloax.fxlib.gametest

import com.mojang.serialization.MapCodec
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.GameTestInstance
import net.minecraft.gametest.framework.TestData
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Rotation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterGameTestsEvent
import com.filloax.fxlib.FxLib

@EventBusSubscriber(modid = FxLib.MOD_ID)
object FxLibNeoGameTests {
    @SubscribeEvent
    @JvmStatic
    fun onRegisterGameTests(event: RegisterGameTestsEvent) {
        val environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(FxLib.MOD_ID, "default")
        )
        val testData = TestData(
            environment,
            Identifier.fromNamespaceAndPath(FxLib.MOD_ID, "empty"),
            100, 0, true, Rotation.NONE
        )
        event.registerTest(
            Identifier.fromNamespaceAndPath(FxLib.MOD_ID, "test_mixins_loaded"),
            object : GameTestInstance(testData) {
                override fun run(helper: GameTestHelper) {
                    FxLibGameTests.testMixinsLoaded(helper)
                }
                override fun codec(): MapCodec<out GameTestInstance> = MapCodec.unit(this)
                override fun typeDescription(): MutableComponent =
                    Component.literal("Mixins Loaded Test")
            }
        )
    }
}
