package com.filloax.fxlib.lang.server

import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component

@Serializable
data class LanguageInfo(val region: String, val name: String, val bidirectional: Boolean)