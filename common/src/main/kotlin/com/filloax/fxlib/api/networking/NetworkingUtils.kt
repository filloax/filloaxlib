package com.filloax.fxlib.api.networking

import com.filloax.fxlib.api.FxLibServices
import io.netty.channel.ChannelFutureListener
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity

fun <T : CustomPacketPayload> ServerPlayer.sendPacket(packet: T, callback: ChannelFutureListener? = null) {
    FxLibServices.networking.sendPacketToPlayer(this, packet, callback)
}

fun <T : CustomPacketPayload> ServerGamePacketListenerImpl.sendPacket(packet: T, callback: ChannelFutureListener ? = null) {
    FxLibServices.networking.sendPacketToPlayer(this.player, packet, callback)
}

fun <T : CustomPacketPayload> Entity.sendPacketToTracking(packet: T, includeSelf: Boolean = false) {
    FxLibServices.networking.sendPacketToTracking(this, packet, includeSelf)
}

fun <T : Any> Entity.setTrackedData(def: TrackedEntityData<T>, value: T, includeSelf: Boolean = false) {
    return def.set(this, value, includeSelf)
}

fun <T : Any> Entity.getTrackedData(def: TrackedEntityData<T>) :T? {
    return def.get(this)
}