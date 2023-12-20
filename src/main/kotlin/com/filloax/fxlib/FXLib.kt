package com.filloax.fxlib

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FXLib : ModInitializer {
    const val MOD_ID = "fxlib"
    const val MOD_NAME = "FXLib"

    @JvmStatic
    val logger: Logger = LogManager.getLogger()

    /**
     * Runs the mod initializer.
     */
    override fun onInitialize() {
        logger.info("Loaded!")
    }
}