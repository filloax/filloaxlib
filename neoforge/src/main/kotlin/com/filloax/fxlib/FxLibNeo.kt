package com.filloax.fxlib

import com.filloax.fxlib.structure.FXLibStructurePlacementTypes
import com.filloax.fxlib.structure.FXLibStructurePoolElements
import com.filloax.fxlib.structure.FXLibStructures
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier


@Mod(FxLib.MOD_ID)
class FxLibNeo(bus: IEventBus) : VersionFxLib() {
    //region registries
    private val registries = mutableListOf<DeferredRegister<*>>()

    private val STRUCTURE_TYPE = createReg(BuiltInRegistries.STRUCTURE_TYPE)
    private val STRUCTURE_PLACEMENT = createReg(BuiltInRegistries.STRUCTURE_PLACEMENT)
    private val STRUCTURE_POOL_ELEMENT = createReg(BuiltInRegistries.STRUCTURE_POOL_ELEMENT)
    //endregion registries

    init {
        initialize()
        registerRegistries(bus)
    }

    override fun initRegistryStructurePlacementType() {
        FXLibStructurePlacementTypes.registerStructurePlacementTypes { id, value ->
            STRUCTURE_PLACEMENT.doRegister(id.path, value)
        }
    }

    override fun initRegistryStructurePoolElementType() {
        FXLibStructurePoolElements.registerStructurePoolElementTypes { id, value ->
            STRUCTURE_POOL_ELEMENT.doRegister(id.path, value)
        }
    }

    override fun initRegistryStructureType() {
        FXLibStructures.registerStructureTypes { id, value ->
            STRUCTURE_TYPE.doRegister(id.path, value)
        }
    }

    private fun <T> DeferredRegister<T>.doRegister(name: String, value: T) {
        register(name, Supplier { value })
    }

    private fun <T> createReg(key: ResourceKey<Registry<T>>): DeferredRegister<T> {
        return DeferredRegister.create(key, MOD_ID).also(registries::add)
    }
    private fun <T> createReg(builtin: Registry<T>): DeferredRegister<T> {
        return DeferredRegister.create(builtin, MOD_ID).also(registries::add)
    }

    private fun registerRegistries(bus: IEventBus) {
        registries.forEach { it.register(bus) }
    }
}