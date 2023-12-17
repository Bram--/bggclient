package org.audux.bgg.request

import org.audux.bgg.BggClient

class Request<T>(private val client: BggClient, private val request: suspend () -> T) {
    fun call(response: (T) -> Unit) {
        client.call(request, response)
    }
}
