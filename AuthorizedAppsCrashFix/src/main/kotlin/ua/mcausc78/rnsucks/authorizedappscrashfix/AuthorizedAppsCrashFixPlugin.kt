package ua.mcausc78.rnsucks.authorizedappscrashfix

import android.content.Context
import android.widget.TextView
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.api.auth.OAuthScope
import com.discord.views.OAuthPermissionViews


@AliucordPlugin(requiresRestart = false)
class AuthorizedAppsCrashFixPlugin : Plugin() {
    private val log: Logger = Logger("RNSucks.AuthorizedAppsCrashFix")

    override fun start(context: Context) {
        Patcher.addPatch(
            OAuthPermissionViews::class.java.getMethod(
                "a",
                TextView::class.java,
                OAuthScope::class.java
            ),
            Hook {
                if (it.hasThrowable()) {
                    val exc = it.throwable
                    if (exc is OAuthPermissionViews.InvalidScopeException) {
                        val scope = exc.a()

                        log.verbose("Preventing crash (encountered $scope)")
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
