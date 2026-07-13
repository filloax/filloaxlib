package com.filloax.fxlib.platform

import com.filloax.fxlib.FxLib
import com.filloax.fxlib.api.networking.*
import com.filloax.fxlib.networking.DelegatingPacketRegistrator
import io.netty.channel.ChannelFutureListener
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.PacketSendListener
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import net.neoforged.neoforge.server.ServerLifecycleHooks

class FxLibNetworkingNeo : FxLibNetworking {
    @EventBusSubscriber
    companion object {
        val registrator = DelegatingPacketRegistrator()

        @SubscribeEvent
        fun registerPayloadsEvent(event: RegisterPayloadHandlersEvent) {
            FxLib.logger.info("Setting up networking...")
            val registrar = event.registrar(FxLib.MOD_ID)
            registrator.setDelegate(PacketRegistratorNeo(registrar))
        }
    }

    override val packetRegistrator: PacketRegistrator = registrator

    override fun <T : CustomPacketPayload> sendPacketToPlayer(
        player: ServerPlayer,
        payload: T,
        callback: ChannelFutureListener ?
    ) {
        try {
            PacketDistributor.sendToPlayer(player, payload)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun <T : CustomPacketPayload> sendPacketToTracking(entity: Entity, payload: T, includeSelf: Boolean) {
        if (includeSelf) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload)
        } else {
            PacketDistributor.sendToPlayersTrackingEntity(entity, payload)
        }
    }

    // Removed since 1.21.6
//    override fun <T : CustomPacketPayload> sendPacketToServer(payload: T, callback: PacketSendListener?) {
//        try {
//            PacketDistributor.sendToServer(payload)
//            callback?.onSuccess()
//        } catch (e: Exception) {
//            callback?.onFailure()
//            throw e
//        }
//    }

    class PacketRegistratorNeo(private val registrar: PayloadRegistrar): PacketRegistrator {
        override fun <T : CustomPacketPayload> playC2S(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: ToServerContext) -> Unit
        ): PacketRegistrator {
            registrar.playToServer(id, codec) { packet, ctx ->
                handler(packet, ToServerContext(ServerLifecycleHooks.getCurrentServer()!!, ctx.player() as ServerPlayer))
            }
            return this
        }

        override fun <T : CustomPacketPayload> playS2C(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: ToClientContext) -> Unit
        ): PacketRegistrator {
            registrar.playToClient(id, codec) { packet, ctx ->
                handler(packet, ToClientContext(Minecraft.getInstance(), ctx.player() as LocalPlayer))
            }
            return this
        }

        override fun <T : CustomPacketPayload> playTwoWay(
            id: CustomPacketPayload.Type<T>,
            codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
            handler: (packet: T, context: TwoWayContext) -> Unit
        ): PacketRegistrator {
            registrar.playBidirectional(id, codec) { packet, ctx ->
                handler(packet, TwoWayContext(
                    if (ctx.flow() == PacketFlow.CLIENTBOUND) Direction.S2C else Direction.C2S,
                    ctx.player() as LocalPlayer,
                ))
            }
            return this
        }
    }
}