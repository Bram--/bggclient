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
package org.audux.bgg.plugin

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Tests for [ClientRateLimitPlugin] and [ConcurrentRequestLimiter]. */
class ClientRateLimitPluginTest {
    private lateinit var requestLimiter: ConcurrentRequestLimiter
    private var delayedResponse:
        suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        {
            delay(20)
            respondOk("OK")
        }

    @BeforeEach
    fun beforeEach() {
        AtomicSingletonInteger.instance.reset()
    }

    @Test
    fun `Does not enqueue incoming requests that do not exceed the concurrent requests limit`() {
        val client =
            createClient(requestLimit = 3) {
                addHandler(delayedResponse)
                addHandler(delayedResponse)
                addHandler(delayedResponse)
            }

        val jobs = runBlocking {
            // Launch 3 concurrent requests
            val jobs = (1..3).map { launch { client.get("/") } }

            // Minor delay to ensure client.get calls are done
            delay(2)

            // Ensure all 3 requests are in-flight and 1 is queued.
            assertThat(requestLimiter.inFlightRequests.get()).isEqualTo(3)

            jobs
        }

        // After all coroutines have finished ensure 3 requests are made
        val engine = client.engine as MockEngine
        assertThat(engine.requestHistory).hasSize(3)
        assertAllJobsAre(jobs) { !isActive }
        assertAllJobsAre(jobs) { !isCancelled }
        assertAllJobsAre(jobs) { isCompleted }
    }

    @Test
    fun `Enqueues incoming requests that would exceed the concurrent requests limit`() {
        val client =
            createClient(requestLimit = 2) {
                addHandler(delayedResponse)
                addHandler(delayedResponse)
                addHandler(delayedResponse)
            }

        val jobs = runBlocking {
            // Launch 3 concurrent requests
            val jobs = (1..3).map { launch { client.get("/") } }

            // Minor delay to ensure client.get calls are done
            delay(2)

            // Ensure only 2 requests are in-flight
            assertThat(requestLimiter.inFlightRequests.get()).isEqualTo(2)

            jobs
        }

        // After all coroutines have finished ensure 3 requests are made
        val engine = client.engine as MockEngine
        assertThat(engine.requestHistory).hasSize(3)
        assertAllJobsAre(jobs) { !isActive }
        assertAllJobsAre(jobs) { !isCancelled }
        assertAllJobsAre(jobs) { isCompleted }
    }

    @Test
    fun `Enqueues incoming requests that would exceed the concurrent requests limit even for different clients`() {
        val clients =
            listOf(
                createClient(requestLimit = 2) { addHandler(delayedResponse) },
                createClient(requestLimit = 2) { addHandler(delayedResponse) },
                createClient(requestLimit = 2) { addHandler(delayedResponse) },
                createClient(requestLimit = 2) { addHandler(delayedResponse) },
                createClient(requestLimit = 2) { addHandler(delayedResponse) },
            )

        val jobs = runBlocking {
            // Launch 5 concurrent requests
            val jobs = clients.map { client -> launch { client.get("/") } }

            // Minor delay to ensure client.get calls are done
            delay(2)

            // Ensure only 2 requests are in-flight.
            assertThat(requestLimiter.inFlightRequests.get()).isEqualTo(2)

            jobs
        }

        // After all coroutines have finished ensure 3 requests are made
        val totalRequests = clients.map { it.engine as MockEngine }.sumOf { it.requestHistory.size }
        assertThat(totalRequests).isEqualTo(5)
        assertAllJobsAre(jobs) { !isActive }
        assertAllJobsAre(jobs) { !isCancelled }
        assertAllJobsAre(jobs) { isCompleted }
    }

    private fun createClient(
        requestLimit: Int,
        responses: MockEngineConfig.() -> Unit
    ): HttpClient {
        return HttpClient(MockEngine(MockEngineConfig().apply { responses(this) })) {
            install(
                createClientPlugin("ClientRateLimitPlugin") {
                    requestLimiter = ConcurrentRequestLimiter(requestLimit)
                    onRequest { request, _ -> requestLimiter.onNewRequest(request) }
                }
            )
        }
    }

    private fun assertAllJobsAre(jobs: List<Job>, predicate: Job.() -> Boolean) {
        assertThat(jobs.all(predicate)).isTrue()
    }
}
