package com.filloax.fxlib

import com.filloax.fxlib.api.lang.server.ServerLanguageManager
import com.filloax.fxlib.api.neoforge.EventOnce
import com.filloax.fxlib.client.FxLibClientNeo
import com.filloax.fxlib.platform.fxLibEvents
import com.filloax.fxlib.structure.FXLibStructurePlacementTypes
import com.filloax.fxlib.structure.FXLibStructurePoolElements
import com.filloax.fxlib.structure.FXLibStructures
import net.minecraft.client.Minecraft
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.AddServerReloadListenersEvent
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import java.util.function.Supplier
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist

@Mod(FxLib.MOD_ID)
object FxLibNeo : VersionFxLib() {
    //region registries
    private val registries = mutableListOf<DeferredRegister<*>>()

    private val STRUCTURE_TYPE = createReg(BuiltInRegistries.STRUCTURE_TYPE)
    private val STRUCTURE_PLACEMENT = createReg(BuiltInRegistries.STRUCTURE_PLACEMENT)
    private val STRUCTURE_POOL_ELEMENT = createReg(BuiltInRegistries.STRUCTURE_POOL_ELEMENT)
    //endregion registries

    init {
        initialize()

        runForDist(
            clientTarget = {
                MOD_BUS.addListener(FxLibClientNeo::initializeClient)
                //Minecraft.getInstance() // todo: it crashes?
            },
            serverTarget = {
                logger.info("Starting server...")
                "filloaxlib"
            }
        )

        registerRegistries(MOD_BUS)

        FORGE_BUS.addListener<AddServerReloadListenersEvent> { ev ->
            ev.addListener(
            Identifier.fromNamespaceAndPath(MOD_ID, "server_language_manager_listener"),
            ServerLanguageManager.ReloadListener())
        }
    }

    override fun initPlatformCallbacks() {
        fxLibEvents.onServerStopped { server ->
            EventOnce.Callbacks.onServerShutdown(server)
        }
    }

    override fun initRegistryStructurePlacementType() {
        FXLibStructurePlacementTypes.registerStructurePlacementTypes { id, value ->
            STRUCTURE_PLACEMENT.doRegister(id, value)
        }
    }

    override fun initRegistryStructurePoolElementType() {
        FXLibStructurePoolElements.registerStructurePoolElementTypes { id, value ->
            STRUCTURE_POOL_ELEMENT.doRegister(id, value)
        }
    }

    override fun initRegistryStructureType() {
        FXLibStructures.registerStructureTypes { id, value ->
            STRUCTURE_TYPE.doRegister(id, value)
        }
    }

    private fun <T : Any> DeferredRegister<T>.doRegister(name: Identifier, value: T) {
        register(name.path, Supplier { value })
    }

    private fun <T : Any> createReg(key: ResourceKey<Registry<T>>): DeferredRegister<T> {
        return DeferredRegister.create(key, MOD_ID).also(registries::add)
    }
    private fun <T : Any> createReg(builtin: Registry<T>): DeferredRegister<T> {
        return DeferredRegister.create(builtin, MOD_ID).also(registries::add)
    }

    private fun registerRegistries(bus: IEventBus) {
        registries.forEach { it.register(bus) }
    }
}