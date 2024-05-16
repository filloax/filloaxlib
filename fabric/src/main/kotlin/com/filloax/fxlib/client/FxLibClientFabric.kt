package com.filloax.fxlib.client

import net.fabricmc.api.ClientModInitializer

class FxLibClientFabric : ClientModInitializer, FxLibClient() {
    /**
     * Runs the mod initializer on the client environment.
     */
    override fun onInitializeClient() {
        initialize()
    }
}