package com.filloax.fxlib

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

abstract class FxLib {
    /**
     * Runs the mod initializer.
     */
    fun initialize() {
        initRegistries()
        initCallbacks()

        logger.info("Loaded!")
    }

    abstract fun initRegistries()
    abstract fun initCallbacks()

    companion object {
        const val MOD_ID = "filloaxlib"
        const val MOD_NAME = "FilloaxLib"

        @JvmField
        val logger: Logger = LogManager.getLogger()
    }
}