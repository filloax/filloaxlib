package com.filloax.fxlib.api.networking

import com.filloax.fxlib.api.FxLibServices
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

fun <T : CustomPacketPayload> ServerPlayer.sendPacket(packet: T, callback: PacketSendListener? = null) {
    FxLibServices.networking.sendPacketToPlayer(this, packet, callback)
}

fun <T : CustomPacketPayload> ServerGamePacketListenerImpl.sendPacket(packet: T, callback: PacketSendListener? = null) {
    FxLibServices.networking.sendPacketToPlayer(this.player, packet, callback)
}