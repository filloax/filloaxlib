package com.filloax.fxlib.client

import com.filloax.fxlib.FxLib
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(value = FxLib.MOD_ID, dist = [Dist.CLIENT])
class FxLibClientNeo(bus: IEventBus) : FxLibClient() {
    init {
        initialize()
    }
}