package com.filloax.fxlib.client

import com.filloax.fxlib.InternalUtils
import net.minecraft.client.gui.screens.Screen

object InternalUtilsClient {
    fun initClientUtils() {
        InternalUtils.clientInitShiftDownHook { Screen.hasShiftDown() }
    }
}