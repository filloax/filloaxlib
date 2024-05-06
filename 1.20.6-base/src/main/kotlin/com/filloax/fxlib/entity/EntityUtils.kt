package com.filloax.fxlib.entity

import com.filloax.fxlib.interfaces.WithPersistentData
import com.filloax.fxlib.platform.getPlatformAbstractions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.portal.PortalInfo
import kotlin.reflect.KProperty

val platformAbstractions = getPlatformAbstractions()

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
    platformAbstractions.fixedChangeDimension(this, level, target)
}


fun getData(entity: Entity): CompoundTag {
    return (entity as WithPersistentData).`ruins_of_fxlib$getPersistentData`()
}

fun Entity.getPersistData() = getData(this)

fun <T : Any> SynchedEntityData.delegate(accessor: EntityDataAccessor<T>) = SynchedEntityDataDelegateImpl(this, accessor)

class SynchedEntityDataDelegateImpl<T : Any>(val entityData: SynchedEntityData, val accessor: EntityDataAccessor<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return entityData.get(accessor)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        entityData.set(accessor, value)
    }
}