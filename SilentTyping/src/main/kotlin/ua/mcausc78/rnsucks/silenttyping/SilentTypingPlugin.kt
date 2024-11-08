package ua.mcausc78.rnsucks.silenttyping

import android.content.Context
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.commands.ApplicationCommandType
import com.discord.stores.StoreUserTyping

@AliucordPlugin(requiresRestart = false)
class SilentTypingPlugin : Plugin() {
    private val log: Logger = Logger("RNSucks.SilentTyping")
    private var silentlyTyping: Boolean = false

    override fun start(context: Context) {
        commands.registerCommand(
            "is-silently-typing",
            "Tells whether you're silently typing or not"
        ) {
            val content = if (silentlyTyping) {
                "Silent typing is on"
            } else {
                "Silent typing is off"
            }
            CommandsAPI.CommandResult(content, null, false)
        }

        commands.registerCommand(
            "silent-typing", "Toggles whether to disable typing indicator or not", listOf(
                Utils.createCommandOption(
                    ApplicationCommandType.BOOLEAN,
                    "toggle",
                    "Disable or enable"
                ),
            )
        ) { ctx ->
            silentlyTyping = if (ctx.containsArg("toggle")) {
                !silentlyTyping
            } else {
                ctx.getBool("toggle")!!
            }

            val content = if (silentlyTyping) {
                "Toggled silent typing on"
            } else {
                "Toggled silent typing off"
            }
            CommandsAPI.CommandResult(content, null, false)
        }

        patcher.before<StoreUserTyping>("setUserTyping", Long::class.java) { param ->
            if (silentlyTyping) {
                log.info("Preventing typing indicator")
                param.result = null
            }
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
