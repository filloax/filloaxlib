package com.filloax.fxlib.platform

import com.filloax.fxlib.platform.fabric.FabricEvents

interface IPlatformEvents {
    fun initEvents()

    companion object {
        fun get(): IPlatformEvents {
            return FabricEvents()
        }
    }
}