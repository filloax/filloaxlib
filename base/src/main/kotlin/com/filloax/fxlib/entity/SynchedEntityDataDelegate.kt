package com.filloax.fxlib.entity

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import kotlin.reflect.KProperty

class SynchedEntityDataDelegate<T : Any>(val entityData: SynchedEntityData, val accessor: EntityDataAccessor<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return entityData.get(accessor)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        entityData.set(accessor, value)
    }
}