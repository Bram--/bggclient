/**
 * Copyright 2023-2024 Bram Wijnands
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
package org.audux.bgg.response

import co.touchlab.kermit.Logger
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps a successful or erroneous response, if a valid response was given it can be found in [data]
 * otherwise the response can be found as a string in [error].
 *
 * @param T The type of response for example [User]
 * @property error Contains the response body when the response could not be parsed by [T]
 * @property data Contains the wrapped successful response
 */
data class Response<T>(val error: String? = null, val data: T? = null) {
    /** Whether the request was successful or not. */
    fun isSuccess() = error.isNullOrBlank()

    /** Whether the request was erroneous or not. */
    fun isError() = !isSuccess()

    internal companion object {
        /** Create a new response from the given response string, using the [mapper]. */
        suspend inline fun <reified T> from(bodyAsText: String, mapper: ObjectMapper): Response<T> =
            withContext(Dispatchers.Default) {
                try {
                    Response(data = mapper.readValue(bodyAsText, T::class.java))
                } catch (e: JacksonException) {
                    Logger.i("Error parsing response", e)
                    Response(error = bodyAsText)
                }
            }
    }
}
