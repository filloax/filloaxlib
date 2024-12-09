package com.filloax.fxlib.test

import com.filloax.fxlib.api.EventUtil
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.*
import net.minecraft.network.chat.Component

object TestEventOnce {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: CommandSelection) {
        dispatcher.register(literal("eventonce").requires { it.hasPermission(2) }
            .then(literal("tickEnd").executes { ctx ->
                logAtServerTickEnd(ctx)
                1
            })
        )
    }

    fun logAtServerTickEnd(ctx: CommandContext<CommandSourceStack>) {
        EventUtil.runAtServerTickEnd {
            ctx.source.sendSystemMessage(Component.literal("Server tick end!"))
        }
    }
}