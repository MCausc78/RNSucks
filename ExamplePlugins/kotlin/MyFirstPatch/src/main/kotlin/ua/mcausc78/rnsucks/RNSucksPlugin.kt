package ua.mcausc78.rnsucks

import android.content.Context
import android.widget.TextView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.auth.OAuthScope
import com.discord.views.OAuthPermissionViews


@AliucordPlugin(requiresRestart = false)
class RNSucksPlugin : Plugin() {
    override fun start(context: Context) {
        Patcher.addPatch(
            OAuthPermissionViews::class.java.getMethod("a", TextView::class.java, OAuthScope::class.java),
            Hook {
                if (it.hasThrowable()) {
                    val exc = it.throwable
                    if (exc is OAuthPermissionViews.InvalidScopeException) {
                        val scope = exc.a()
                        (it.args[0] as TextView).text = "? '$scope'"
                        // TODO: context.getString(R.string.scope_unknown, scope)
                        it.throwable = null
                    }
                }
            }
        )
    }

    override fun stop(context: Context) {
        // Remove all patches
        patcher.unpatchAll()
    }
}
