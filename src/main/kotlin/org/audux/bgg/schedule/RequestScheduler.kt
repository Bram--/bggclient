package org.audux.bgg.schedule

import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.request.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RequestScheduler() : KoinComponent {
    private val bggClient: BggClient by inject()

    fun <R> invoke(request: R, response: (R) -> Unit) where R : Request {
        runBlocking { response(bggClient.call { request }) }
    }

    fun <R> schedule(delay: Long? = null, request: R, response: (R) -> Unit) where R : Request {}
}
