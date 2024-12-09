package com.filloax.fxlib.test

import com.filloax.fxlib.api.EventUtil
import com.filloax.fxlib.api.FxLibServices
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.*
import net.minecraft.network.chat.Component

object TestServerLang {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, registryAccess: CommandBuildContext, environment: CommandSelection) {
        dispatcher.register(literal("serverlang").requires { it.hasPermission(2) }
            .then(literal("get").then(argument("lang", StringArgumentType.string())
                .then(argument("key", StringArgumentType.string())
                    .executes { ctx ->
                        sendServerString(ctx, StringArgumentType.getString(ctx, "lang"), StringArgumentType.getString(ctx, "key"))
                        1
                    })
            ))
        )
    }

    fun sendServerString(ctx: CommandContext<CommandSourceStack>, lang: String, key: String) {
        ctx.source.sendSystemMessage(Component.literal(FxLibServices.serverLanguage.get(lang).getOrDefault(key)))
    }
}