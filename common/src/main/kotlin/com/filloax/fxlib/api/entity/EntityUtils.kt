package com.filloax.fxlib.api.entity

import com.filloax.fxlib.entity.SynchedEntityDataDelegate
import com.filloax.fxlib.api.interfaces.WithPersistentData
import com.filloax.fxlib.api.platform.getPlatformAbstractions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.level.portal.DimensionTransition

private val platformAbstractions = getPlatformAbstractions()

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
@Deprecated("Superseded by entity.changeDimension in 1.21, keep for backwards compat")
fun Entity.fixedChangeDimension(level: ServerLevel, target: DimensionTransition) {
    changeDimension(target)
}

fun getData(entity: Entity): CompoundTag {
    return (entity as WithPersistentData).`ruins_of_fxlib$getPersistentData`()
}

fun Entity.getPersistData() = getData(this)

fun <T : Any> SynchedEntityData.delegate(accessor: EntityDataAccessor<T>) = SynchedEntityDataDelegate(this, accessor)

inline fun <reified T : Entity> entityTestForClass(): EntityTypeTest<Entity, T> {
    return EntityTypeTest.forClass(T::class.java)
}
