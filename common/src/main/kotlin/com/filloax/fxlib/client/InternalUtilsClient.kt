package com.filloax.fxlib.client

import com.filloax.fxlib.InternalUtils
import net.minecraft.client.Minecraft

object InternalUtilsClient {
    fun initClientUtils() {
        InternalUtils.clientInitShiftDownHook { Minecraft.getInstance().hasShiftDown() }
    }
}