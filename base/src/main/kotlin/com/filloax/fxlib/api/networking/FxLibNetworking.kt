package com.filloax.fxlib.api.networking

import com.filloax.fxlib.api.platform.ServiceUtil
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

interface FxLibNetworking {
    /**
     * Still needs to register the packets (aka the T:CustomPacketPayload used) as needed in the loader of choice
     */
    fun <T : CustomPacketPayload> sendPacketToPlayer(player: ServerPlayer, payload: T, callback: PacketSendListener? = null)
    /**
     * Still needs to register the packets (aka the T:CustomPacketPayload used) as needed in the loader of choice
     */
    fun <T : CustomPacketPayload> sendPacketToServer(payload: T, callback: PacketSendListener? = null)

    companion object {
        val inst by lazy { ServiceUtil.findService(FxLibNetworking::class.java) }
    }
}