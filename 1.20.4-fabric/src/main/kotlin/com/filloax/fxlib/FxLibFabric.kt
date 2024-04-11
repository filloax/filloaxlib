package com.filloax.fxlib

import net.fabricmc.api.ModInitializer

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