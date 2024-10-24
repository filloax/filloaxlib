package com.filloax.fxlib.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest

object BaseEntityTypeTest : EntityTypeTest<Entity, Entity> {
    override fun tryCast(entity: Entity): Entity {
        return entity
    }

    override fun getBaseClass(): Class<out Entity> {
        return Entity::class.java
    }
}