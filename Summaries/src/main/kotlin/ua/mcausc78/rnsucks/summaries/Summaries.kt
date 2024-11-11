package ua.mcausc78.rnsucks.summaries

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.api.GatewayAPI
import com.aliucord.entities.Plugin
import com.aliucord.utils.SerializedName
import com.aliucord.wrappers.ChannelWrapper.Companion.name
import com.discord.stores.StoreStream

data class Summary(
    val id: Long,
    @SerializedName("end_id")
    val endId: Long,
    val count: Int,
    @SerializedName("message_ids")
    val messageIds: List<Long>,
    val people: List<Long>,
    val source: Int,
    @SerializedName("start_id")
    val startId: Long,
    @SerializedName("summ_short")
    val summShort: String,
    val topic: String,
    val type: Int,
    val unsafe: Boolean,
)

data class ConversationSummaryUpdate(
    @SerializedName("channel_id")
    val channelId: Long,
    @SerializedName("guild_id")
    val guildId: Long,
    val summaries: List<Summary>,
)

@AliucordPlugin(requiresRestart = false)
class SummariesPlugin : Plugin() {
    private val summaries: MutableMap<Long, MutableList<Summary>> = mutableMapOf()

    override fun start(context: Context) {
        // {
        //     "channel_id": "125227483518861312",
        //     "guild_id": "125227483518861312",
        //     "summaries": [
        //         {
        //             "count": 3,
        //             "end_id": "1305299001427628052",
        //             "id": "1305306704509927516",
        //             "message_ids": [
        //                 "1305298903226126346",
        //                 "1305298947706851410",
        //                 "1305299001427628052"
        //             ],
        //             "people": [
        //                 "222046562543468545",
        //                 "198341984346308609"
        //             ],
        //             "source": 2,
        //             "start_id": "1305298903226126346",
        //             "summ_short": "Alessandro has updated the dependencies but still gets the same error when upgrading from previous versions.",
        //             "topic": "Log4j 2.24.1 Upgrade",
        //             "type": 3,
        //             "unsafe": false
        //         }
        //     ]
        // }
        GatewayAPI.onEvent<ConversationSummaryUpdate>("CONVERSATION_SUMMARY_UPDATE") { event ->
            val channelId = event.channelId
            val guildId = event.guildId

            val channel = StoreStream.getChannels().getChannel(channelId)
            if (channel == null) {
                logger.warn("CONVERSATION_SUMMARY_UPDATE referencing an unknown channel ID: $channelId. Updating the cache anyway.")
            }

            val guild = StoreStream.getGuilds().getGuild(guildId)

            val channelName = channel?.name ?: "?"
            val guildName = guild?.name ?: "?"

            if (event.summaries.isEmpty()) {
                logger.verbose("No summaries were added to $channelName (ID: $channelId) in $guildName (ID: $guildId)")
            } else {
                logger.verbose("Summaries updated in $channelName (ID: $channelId) in $guildName (ID: $guildId)")
            }

            val summaries = this.summaries[channelId]
            if (summaries == null) {
                this.summaries[channelId] = event.summaries.toMutableList()
            } else {
                val summaryIds = summaries.map { it.id }

                for (summary in event.summaries) {
                    val index = summaryIds.indexOf(summary.id)
                    if (index == -1) {
                        // new summary
                        summaries.add(summary)
                    } else {
                        // update summary (?)
                        // TODO: figure out if CONVERSATION_SUMMARY_UPDATE is just dispatched when a summary is added?
                        summaries[index] = summary
                    }
                }

                summaries.sortBy { it.id.toLong() }
            }
        }

        commands.registerCommand("summaries", "Shows current summaries") { ctx ->
            val summaries = this.summaries[ctx.currentChannel.id]
            if (summaries.isNullOrEmpty()) {
                return@registerCommand CommandsAPI.CommandResult(
                    "No summaries in channel currently.",
                    null,
                    false
                )
            }
            var result = "Summaries:\n"
            for (summary in summaries) {
                result += "- **${summary.topic}**: ${summary.summShort} (${summary.messageIds.size} messages and ${summary.people.size} people involved)\n"
            }
            CommandsAPI.CommandResult(result, null, false)
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
