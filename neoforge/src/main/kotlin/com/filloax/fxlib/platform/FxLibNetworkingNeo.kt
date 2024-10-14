package com.filloax.fxlib.platform

import com.filloax.fxlib.api.networking.FxLibNetworking
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor

class FxLibNetworkingNeo : FxLibNetworking {
    override fun <T : CustomPacketPayload> sendPacketToPlayer(
        player: ServerPlayer,
        payload: T,
        callback: PacketSendListener?
    ) {
        try {
            PacketDistributor.sendToPlayer(player, payload)
            callback?.onSuccess()
        } catch (e: Exception) {
            callback?.onFailure()
            throw e
        }
    }

    override fun <T : CustomPacketPayload> sendPacketToServer(payload: T, callback: PacketSendListener?) {
        try {
            PacketDistributor.sendToServer(payload)
            callback?.onSuccess()
        } catch (e: Exception) {
            callback?.onFailure()
            throw e
        }
    }
}