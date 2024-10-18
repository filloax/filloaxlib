package com.filloax.fxlib.networking

import com.filloax.fxlib.FxLib
import com.filloax.fxlib.api.networking.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class DelegatingPacketRegistrator() : PacketRegistrator {
    private val queuedCalls = mutableListOf<(PacketRegistrator) -> Unit>()
    private var delegate: PacketRegistrator? = null

    fun setDelegate(value: PacketRegistrator) {
        if (delegate != null) {
            delegate = value
            queuedCalls.forEach { it(value) }
            queuedCalls.clear()
        } else {
            FxLib.logger.error("Cannot re-set delegate for DelegatingPacketRegistrator!")
        }
    }

    override fun <T : CustomPacketPayload> playC2S(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: ToServerContext) -> Unit
    ): PacketRegistrator {
        delegate?.playC2S(id, codec, handler) ?: run {
            queuedCalls.add { it.playC2S(id, codec, handler) }
        }
        return this
    }

    override fun <T : CustomPacketPayload> playS2C(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: ToClientContext) -> Unit
    ): PacketRegistrator {
        delegate?.playS2C(id, codec, handler) ?: run {
            queuedCalls.add { it.playS2C(id, codec, handler) }
        }
        return this
    }

    override fun <T : CustomPacketPayload> playTwoWay(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: TwoWayContext) -> Unit
    ): PacketRegistrator {
        delegate?.playTwoWay(id, codec, handler) ?: run {
            queuedCalls.add { it.playTwoWay(id, codec, handler) }
        }
        return this
    }
}