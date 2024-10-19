package com.filloax.fxlib.client

import com.filloax.fxlib.FxLib
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent

object FxLibClientNeo : FxLibClient() {
    fun initializeClient(event: FMLClientSetupEvent) {
        FxLib.logger.info("Initializing client...")
        initialize()
    }
}