package ua.mcausc78.rnsucks.noblockedmessages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.commands.ApplicationCommandType
import com.discord.api.user.TypingUser
import com.discord.databinding.WidgetChatListAdapterItemBlockedBinding
import com.discord.models.domain.ModelUserRelationship
import com.discord.stores.StoreStream
import com.discord.stores.StoreUserRelationships
import com.discord.stores.StoreUserTyping
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemBlocked
import com.discord.widgets.chat.list.entries.ChatListEntry


@AliucordPlugin(requiresRestart = false)
class NoBlockedMessagesPlugin : Plugin() {
    private val log: Logger = Logger("RNSucks.NoBlockedMessagesPlugin")

    override fun start(context: Context) {
        val blockedBinding =
            WidgetChatListAdapterItemBlocked::class.java.getDeclaredField("binding").apply {
                isAccessible = true
            }
        val ensureRelationshipLoaded =
            StoreUserRelationships::class.java.getDeclaredMethod("ensureRelationshipLoaded").apply {
                isAccessible = true
            }

        commands.registerCommand(
            "local-block", "Blocks a user locally", listOf(
                Utils.createCommandOption(
                    ApplicationCommandType.USER,
                    "target",
                    "The user to block",
                    required = true,
                    default = true,
                ),
            )
        ) { ctx ->
            val target = ctx.getRequiredUser("target")

            val relationshipStore = StoreStream.getUserRelationships()
            val oldRelationship =
                relationshipStore.relationships[target.id] ?: ModelUserRelationship.TYPE_NONE

            if (oldRelationship == ModelUserRelationship.TYPE_BLOCKED) {
                return@registerCommand CommandsAPI.CommandResult(
                    "You already have blocked `${target.username}`.",
                    null,
                    false
                )
            }

            (ensureRelationshipLoaded.invoke(relationshipStore) as StoreUserRelationships.UserRelationshipsState.Loaded).relationships[target.id] =
                ModelUserRelationship.TYPE_BLOCKED
            relationshipStore.markChanged()

            val content = when (oldRelationship) {
                ModelUserRelationship.TYPE_NONE -> "Blocked `${target.username}` locally."
                ModelUserRelationship.TYPE_FRIEND -> "You were friends with `${target.username}`, but now they are blocked locally."
                ModelUserRelationship.TYPE_INVITE_INCOMING -> "`${target.username}` have sent friend request to you, now you blocked them in response."
                ModelUserRelationship.TYPE_INVITE_OUTGOING -> "You had sent a friend request to `${target.username}`, but you blocked them."
                else -> "How you had relationship with `${target.username}` of type $oldRelationship?"
            }

            CommandsAPI.CommandResult(content, null, false)
        }

        commands.registerCommand(
            "local-unblock", "Unblocks a user locally", listOf(
                Utils.createCommandOption(
                    ApplicationCommandType.USER,
                    "target",
                    "The user to unblock",
                    required = true,
                    default = true,
                ),
            )
        ) { ctx ->
            val target = ctx.getRequiredUser("target")

            val relationshipStore = StoreStream.getUserRelationships()
            val relationships = relationshipStore.relationships
            val oldRelationship = relationships[target.id] ?: 0

            if (oldRelationship != ModelUserRelationship.TYPE_BLOCKED) {
                return@registerCommand CommandsAPI.CommandResult(
                    "You didn't had `${target.username}` blocked.",
                    null,
                    false
                )
            }

            (ensureRelationshipLoaded.invoke(relationshipStore) as StoreUserRelationships.UserRelationshipsState.Loaded).relationships[target.id] =
                ModelUserRelationship.TYPE_NONE
            relationshipStore.markChanged()

            CommandsAPI.CommandResult("Unblocked `${target.username}` locally.", null, false)
        }

        patcher.before<StoreUserTyping>("handleTypingStart", TypingUser::class.java) { param ->
            val event = param.args[0] as TypingUser

            val relationships = StoreStream.getUserRelationships().relationships

            val userId = event.d()
            val isBlocked =
                (relationships[userId] ?: return@before) == ModelUserRelationship.TYPE_BLOCKED
            if (isBlocked) {
                val au = event.c()?.m()
                val tag = if (au == null) {
                    val mu = StoreStream.getUsers().users[event.d()]!!
                    "${mu.username}#${mu.discriminator}"
                } else {
                    "${au.username}#${au.f()}"
                }
                log.verbose("User $tag started typing in ${event.a()} but they are blocked")
                // cancel cache updating
                param.result = null
            }
        }

        patcher.instead<WidgetChatListAdapterItemBlocked>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { param ->
            val root =
                (blockedBinding.get(param.thisObject) as WidgetChatListAdapterItemBlockedBinding).root
            root.visibility = View.GONE

            val lp = LayoutParams(0, 0)
            root.layoutParams = lp

            null
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
        patcher.unpatchAll()
    }
}
