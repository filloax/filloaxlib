package com.filloax.fxlib.platform

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.portal.PortalInfo

/**
 * Description adapted from Fabric API, applies here too mostly
 * Teleports an entity to a different dimension, placing it at the specified destination.
 *
 * <p>Using this method will circumvent Vanilla's portal placement code.
 *
 * <p>When teleporting to another dimension, the entity may be replaced with a new entity in the target
 * dimension. This is not the case for players, but needs to be accounted for by the caller.
 *
 * @param destination the dimension the entity will be teleported to
 * @param target      where the entity will be placed in the target world.
 *                    As in Vanilla, the target's velocity is not applied to players.
 * @param <E>         the type of the teleported entity
 * @return Returns the teleported entity in the target dimension, which may be a new entity or <code>teleported</code>,
 * depending on the entity type.
 * @apiNote this method must be called from the main server thread
 */
fun Entity.fixedChangeDimension(level: ServerLevel, target: PortalInfo) {
    FabricDimensions.teleport(this, level, target)
}