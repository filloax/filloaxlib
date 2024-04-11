package com.filloax.fxlib.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest

inline fun <reified T : Entity> entityTestForClass(): EntityTypeTest<Entity, T> {
    return EntityTypeTest.forClass(T::class.java)
}

object BaseEntityTypeTest : EntityTypeTest<Entity, Entity> {
    override fun tryCast(entity: Entity): Entity {
        return entity
    }

    override fun getBaseClass(): Class<out Entity> {
        return Entity::class.java
    }
}