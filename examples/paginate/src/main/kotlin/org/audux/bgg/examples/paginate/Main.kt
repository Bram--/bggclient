package org.audux.bgg.examples.paginate

import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.BggClient.Severity
import org.audux.bgg.common.Inclusion
import org.audux.bgg.response.GuildMember
import java.util.concurrent.CopyOnWriteArrayList

class Main {

    object Companion {
        @JvmStatic
        fun main(vararg args: String) {
            // Sets the logger level for the client - default WARN.
            BggClient.setLoggerSeverity(Severity.Verbose)
            val members = CopyOnWriteArrayList<GuildMember>()

            runBlocking {
                val completeGuild = BggClient.guilds(id = 2611, members = Inclusion.INCLUDE)

            }
        }
    }
}