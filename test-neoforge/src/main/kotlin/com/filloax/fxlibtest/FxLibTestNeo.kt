package com.filloax.fxlibtest

import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(FxLibTestNeo.MOD_ID)

object FxLibTestNeo {
    const val MOD_ID = "filloaxlibtest"

    val logger: Logger = LogManager.getLogger()

    init {
       logger.info("Initialized test mod!")
    }
}