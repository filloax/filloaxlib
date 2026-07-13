package com.filloax.fxlib.api.networking

import com.filloax.fxlib.api.FxLibServices
import com.filloax.fxlib.platform.fxLibEvents
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

/**
 * Attaches a piece of data to any entity that automatically syncs to every client tracking the entity
 * when it changes, and to a client the moment it starts tracking the entity.
 *
 * Construct once per kind of data (each instance is its own packet channel) and call [register] during
 * mod init before packets are finalized, mirroring how PacketRegistrator entries are registered.
 *
 * [T] must be non-null, use `Optional<F>` if needed. "No value known yet" (as opposed to "value known
 * to be absent") is represented as an absent map entry.
 */
class TrackedEntityData<T : Any>(
    id: Identifier,
    valueCodec: StreamCodec<in RegistryFriendlyByteBuf, T>,
) {
    private val payloadType = CustomPacketPayload.Type<Packet<T>>(id)
    private val packetCodec: StreamCodec<RegistryFriendlyByteBuf, Packet<T>> = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, Packet<T>::entityId,
        valueCodec, Packet<T>::value,
    ) { entityId, value -> Packet(entityId, value, payloadType) }

    private val serverValues = mutableMapOf<Int, T>()
    private val clientValues = mutableMapOf<Int, T>()

    fun register(): TrackedEntityData<T> {
        FxLibServices.networking.packetRegistrator.playS2C(payloadType, packetCodec) { packet, _ ->
            clientValues[packet.entityId] = packet.value
        }
        fxLibEvents.onStartTrackingEntity { entity, player ->
            serverValues[entity.id]?.let { player.sendPacket(Packet(entity.id, it, payloadType)) }
        }
        return this
    }

    /** Server-side: set the current value and broadcast it to everyone tracking [entity] (+ itself if [includeSelf]). */
    fun set(entity: Entity, value: T, includeSelf: Boolean = false) {
        serverValues[entity.id] = value
        entity.sendPacketToTracking(Packet(entity.id, value, payloadType), includeSelf)
    }

    /** Server-side: last value set for this entity. Client-side: last value received (or null if never synced). */
    fun get(entity: Entity): T? =
        if (entity.level().isClientSide) clientValues[entity.id] else serverValues[entity.id]

    fun remove(entity: Entity) {
        serverValues.remove(entity.id)
    }

    private class Packet<T>(
        val entityId: Int,
        val value: T,
        private val payloadType: CustomPacketPayload.Type<Packet<T>>,
    ) : CustomPacketPayload {
        override fun type() = payloadType
    }
}
