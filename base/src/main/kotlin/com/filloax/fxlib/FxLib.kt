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
        @JvmField
        val MOD_ID = "fxlib"
        @JvmField
        val MOD_NAME = "FXLib"

        @JvmField
        val logger: Logger = LogManager.getLogger()
    }
}