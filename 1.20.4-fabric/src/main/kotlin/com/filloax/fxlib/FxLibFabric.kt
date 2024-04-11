package com.filloax.fxlib

import com.filloax.fxlib.platform.IPlatformEvents
import com.filloax.fxlib.structure.FXLibStructures
import net.fabricmc.api.ModInitializer
import net.minecraft.core.registries.BuiltInRegistries

object FxLibFabric : ModInitializer, FxLib() {
    const val MOD_ID = "fxlib"
    const val MOD_NAME = "FXLib"

    /**
     * Runs the mod initializer.
     */
    override fun onInitialize() {
        initialize()
    }

    override fun initRegistries() {
        TODO("Not yet implemented")
    }

    override fun initCallbacks() {
        TODO("Not yet implemented")
    }
}