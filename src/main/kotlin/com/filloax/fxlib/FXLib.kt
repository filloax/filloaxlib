package com.filloax.fxlib

import com.filloax.fxlib.structure.FXLibStructures
import net.fabricmc.api.ModInitializer
import net.minecraft.core.registries.BuiltInRegistries
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
        FXLibStructures.init(BuiltInRegistries.STRUCTURE_TYPE)


        logger.info("Loaded!")
    }
}