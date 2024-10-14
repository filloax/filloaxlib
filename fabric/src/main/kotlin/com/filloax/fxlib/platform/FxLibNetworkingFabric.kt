package com.filloax.fxlib.platform

import com.filloax.fxlib.api.networking.FxLibNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

class FxLibNetworkingFabric : FxLibNetworking {
    override fun <T : CustomPacketPayload> sendPacketToPlayer(
        player: ServerPlayer,
        payload: T,
        callback: PacketSendListener?
    ) {
        ServerPlayNetworking.getSender(player).sendPacket(payload, callback)
    }

    override fun <T : CustomPacketPayload> sendPacketToServer(payload: T, callback: PacketSendListener?) {
        try {
            ClientPlayNetworking.send(payload)
            callback?.onSuccess()
        } catch (e: Exception) {
            callback?.onFailure()
            throw e
        }
    }
}