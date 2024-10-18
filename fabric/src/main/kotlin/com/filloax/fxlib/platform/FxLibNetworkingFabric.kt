package com.filloax.fxlib.platform

import com.filloax.fxlib.api.networking.*
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketSendListener
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

class FxLibNetworkingFabric : FxLibNetworking {
    override val packetRegistrator: PacketRegistrator = PacketRegistratorFabric()

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

    class PacketRegistratorFabric : PacketRegistrator {
        val payloadRegistryS2C = PayloadTypeRegistry.playS2C()
        val payloadRegistryC2S = PayloadTypeRegistry.playC2S()

        override fun <T : CustomPacketPayload> playC2S(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: ToServerContext) -> Unit
        ): PacketRegistrator {
            payloadRegistryC2S.register(id, codec)
            ServerPlayNetworking.registerGlobalReceiver(id) { packet, ctx ->
                handler(packet, ToServerContext(ctx.server(), ctx.player()))
            }
            return this
        }

        override fun <T : CustomPacketPayload> playS2C(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: ToClientContext) -> Unit
        ): PacketRegistrator {
            payloadRegistryS2C.register(id, codec)
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(id) { packet, ctx ->
                    handler(packet, ToClientContext(ctx.client(), ctx.player()))
                }
            }
            return this
        }

        override fun <T : CustomPacketPayload> playTwoWay(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: TwoWayContext) -> Unit
        ): PacketRegistrator {
            payloadRegistryS2C.register(id, codec)
            payloadRegistryC2S.register(id, codec)
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(id) { packet, ctx ->
                    handler(packet, TwoWayContext(Direction.S2C, ctx.player()))
                }
            }
            ServerPlayNetworking.registerGlobalReceiver(id) { packet, ctx ->
                handler(packet, TwoWayContext(Direction.C2S, ctx.player()))
            }
            return this
        }
    }
}