/**
 * Copyright 2023 Bram Wijnands
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.audux.bgg.request

import org.audux.bgg.BggClient

/** Encapsulates a request to BGG so it can be scheduled or queued for later execution. */
class Request<T>(private val client: BggClient, private val request: suspend () -> T) {
    /**
     * Execute the encapsulated [T] request asynchronously and returns the `response` in the
     * provided block if successful.
     */
    fun callAsync(response: (T) -> Unit) {
        client.callAsync(request, response)
    }

    /** Execute the encapsulated [T] request and returns [T] if successful. */
    suspend fun call() = client.call(request)
}