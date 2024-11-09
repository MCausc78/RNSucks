package ua.mcausc78.rnsucks.intlhasher

import android.content.Context
import com.joom.xxhash.XxHash64
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2

const val intlBase64Table: String =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

fun numberToBytes(number: Long): List<Byte> {
    val result = mutableListOf<Byte>()
    val count = ceil(floor(log2(number.toDouble()) + 1) / 8).toInt()
    for (i in 0..count) {
        result.add(0, ((number shr (8 * i)) and 0xff).toByte())
    }
    result.reverse()
    return result
}

fun intlHash(key: String): String {
    val data = numberToBytes(XxHash64.hashForArray(key.encodeToByteArray()))
    val d0 = data[0].toInt()
    val d1 = data[1].toInt()
    val d2 = data[2].toInt()
    val d3 = data[3].toInt()

    return (
            intlBase64Table[d0 shr 2].toString() +
                    intlBase64Table[((d0 and 0x03) shl 4) or (d1 shr 4)].toString() +
                    intlBase64Table[((d1 and 0x0f) shl 2) or (d2 shr 6)].toString() +
                    intlBase64Table[d2 and 0x3f].toString() +
                    intlBase64Table[d3 shr 2].toString() +
                    intlBase64Table[((d3 and 0x03) shl 4) or (d3 shr 4)].toString()
            )
}


@AliucordPlugin(requiresRestart = false)
class IntlHasherPlugin : Plugin() {
    override fun start(context: Context) {
        commands.registerCommand(
            "intl-hash", "Hashes a string", listOf(
                Utils.createCommandOption(
                    ApplicationCommandType.STRING,
                    "string",
                    "The string to hash",
                    required = true,
                ),
            )
        ) { ctx ->
            CommandsAPI.CommandResult(intlHash(ctx.getRequiredString("string")), null, false)
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
