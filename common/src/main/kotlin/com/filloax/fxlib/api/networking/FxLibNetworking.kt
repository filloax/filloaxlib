package com.filloax.fxlib.api.networking

import com.filloax.fxlib.api.platform.ServiceUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.PacketSendListener
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player


interface FxLibNetworking {
    val packetRegistrator: PacketRegistrator

    /**
     * Still needs to register the packets (aka the T:CustomPacketPayload used)
     */
    fun <T : CustomPacketPayload> sendPacketToPlayer(player: ServerPlayer, payload: T, callback: PacketSendListener? = null)
    /**
     * Still needs to register the packets (aka the T:CustomPacketPayload used)
     */
    fun <T : CustomPacketPayload> sendPacketToServer(payload: T, callback: PacketSendListener? = null)

    companion object {
        val inst by lazy { ServiceUtil.findService(FxLibNetworking::class.java) }

        fun <T : CustomPacketPayload> twoWayHandler(
            c2s: (packet: T, context: ToServerContext) -> Unit,
            s2c: (packet: T, context: ToClientContext) -> Unit,
        ): (packet: T, context: TwoWayContext) -> Unit {
            return { packet, context ->
                if (context.direction == Direction.S2C) {
                    s2c(packet, ToClientContext(Minecraft.getInstance(), context.player as LocalPlayer))
                } else {
                    val serverPlayer = context.player as ServerPlayer
                    c2s(packet, ToServerContext(serverPlayer.server, serverPlayer))
                }
            }
        }
    }
}

interface PacketRegistrator {
    fun <T : CustomPacketPayload> playC2S(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: ToServerContext) -> Unit
    ): PacketRegistrator

    fun <T : CustomPacketPayload> playS2C(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: ToClientContext) -> Unit
    ): PacketRegistrator

    fun <T : CustomPacketPayload> playTwoWay(
        id: CustomPacketPayload.Type<T>,
        codec: StreamCodec<in RegistryFriendlyByteBuf, T>,
        handler: (packet: T, context: TwoWayContext) -> Unit
    ): PacketRegistrator
}

enum class Direction {
    C2S,
    S2C,
    ;
}

sealed class PacketContext(open val player: Player)
data class ToServerContext(val server: MinecraftServer, override val player: ServerPlayer) : PacketContext(player)
data class ToClientContext(val client: Minecraft, override val player: LocalPlayer) : PacketContext(player)
data class TwoWayContext(val direction: Direction, override val player: Player) : PacketContext(player)


fun <T : CustomPacketPayload> PacketRegistrator.playC2S(
    entry: TypeAndCodec<in RegistryFriendlyByteBuf, T>,
    handler: (packet: T, context: ToServerContext) -> Unit
) = playC2S(entry.type, entry.codec, handler)
fun <T : CustomPacketPayload> PacketRegistrator.playS2C(
    entry: TypeAndCodec<in RegistryFriendlyByteBuf, T>,
    handler: (packet: T, context: ToClientContext) -> Unit
) = playS2C(entry.type, entry.codec, handler)
fun <T : CustomPacketPayload> PacketRegistrator.playTwoWay(
    entry: TypeAndCodec<in RegistryFriendlyByteBuf, T>,
    handler: (packet: T, context: TwoWayContext) -> Unit
) = playTwoWay(entry.type, entry.codec, handler)
