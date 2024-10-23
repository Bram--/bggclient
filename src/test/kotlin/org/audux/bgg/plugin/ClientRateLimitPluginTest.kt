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
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.get
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.util.TestUtils.instantResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Tests for [ClientConcurrentRateLimitPlugin] and [ConcurrentRequestLimiter]. */
class ClientRateLimitPluginTest {
  private lateinit var requestLimiter: RequestLimiter

  @BeforeEach
  fun beforeEach() {
    AtomicSingletonInteger.instance.reset()
  }

  @Test
  fun `Enqueue incoming requests that do not exceed the request limit for the current window`() {
    val client = createClient(requestLimit = 10) { repeat(4) { addHandler(instantResponse()) } }

    val jobs = runBlocking {
      // Launch 4 requests.
      val jobs = (0..3).map { launch { client.get("/") } }

      delay(2)

      // Ensure 2 requests are in-flight and 2 are queued.
      assertThat(requestLimiter.requestsInCurrentWindow.get()).isEqualTo(4)

      jobs
    }

    // After all coroutines have finished ensure 3 requests are made
    val engine = client.engine as MockEngine
    assertThat(engine.requestHistory).hasSize(4)
    assertAllJobsAre(jobs) { !isActive }
    assertAllJobsAre(jobs) { !isCancelled }
    assertAllJobsAre(jobs) { isCompleted }
  }

  @Test
  fun `Does not enqueue incoming requests that exceed the request limit for the current window`() {
    val client = createClient(requestLimit = 2) { repeat(4) { addHandler(instantResponse()) } }

    val jobs = runBlocking {
      // Launch 4 requests.
      val jobs = (0..3).map { launch { client.get("/") } }

      delay(2)

      // Ensure 2 requests are in-flight and 2 are queued.
      assertThat(requestLimiter.requestsInCurrentWindow.get()).isEqualTo(2)

      jobs
    }

    // After all coroutines have finished ensure 3 requests are made
    val engine = client.engine as MockEngine
    assertThat(engine.requestHistory).hasSize(4)
    assertAllJobsAre(jobs) { !isActive }
    assertAllJobsAre(jobs) { !isCancelled }
    assertAllJobsAre(jobs) { isCompleted }
  }

  @RepeatedTest(100)
  fun `delays requests to the new window`() {
    val client = createClient(requestLimit = 2) { repeat(5) { addHandler(instantResponse()) } }

    runBlocking {
      val job1 = launch { client.get("/1") }
      val job2 = launch { client.get("/2") }
      delay(2) // ensure jobs are running

      val job3 = launch { client.get("/3") }
      val job4 = launch { client.get("/4") }
      val job5 = launch { client.get("/5") }
      assertThat(requestLimiter.requestsInCurrentWindow.get()).isEqualTo(2)

      delay(10) // ensure jobs 1 and 2 are completed

      assertThat(job1.isCompleted).isTrue()
      assertThat(job2.isCompleted).isTrue()
      assertThat(job3.isActive).isTrue()
      assertThat(job4.isActive).isTrue()
      assertThat(job5.isActive).isTrue()

      delay(20) // ensure jobs 3 and 4 are completed

      assertThat(job1.isCompleted).isTrue()
      assertThat(job2.isCompleted).isTrue()
      assertThat(job3.isCompleted).isTrue()
      assertThat(job4.isCompleted).isTrue()
      assertThat(job5.isActive).isTrue()

      delay(30) // ensure job 5 is completed.

      assertThat(job5.isCompleted).isTrue()
    }

    val engine = client.engine as MockEngine
    assertThat(engine.requestHistory).hasSize(5)
  }

  @Test
  fun `Enqueues incoming requests that would exceed the requests limit even for different clients`() {
    val clients =
      listOf(
        createClient(requestLimit = 2) { addHandler(instantResponse()) },
        createClient(requestLimit = 2) { addHandler(instantResponse()) },
        createClient(requestLimit = 2) { addHandler(instantResponse()) },
        createClient(requestLimit = 2) { addHandler(instantResponse()) },
        createClient(requestLimit = 2) { addHandler(instantResponse()) },
      )

    val jobs = runBlocking {
      // Launch 5 concurrent requests
      val jobs = clients.map { client -> launch { client.get("/") } }

      // Minor delay to ensure client.get calls are done
      delay(2)

      // Ensure 2 requests are in-flight and 3 are queued.
      assertThat(requestLimiter.requestsInCurrentWindow.get()).isEqualTo(2)

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
    windowSize: Duration = 20.milliseconds,
    responses: MockEngineConfig.() -> Unit,
  ): HttpClient {
    return HttpClient(MockEngine(MockEngineConfig().apply { responses(this) })) {
      install(
        createClientPlugin("ClientRateLimitPlugin") {
          requestLimiter = RequestLimiter(requestLimit, windowSize)
          onRequest { request, _ -> requestLimiter.onNewRequest(request) }
        }
      )
    }
  }

  private fun assertAllJobsAre(jobs: List<Job>, predicate: Job.() -> Boolean) {
    assertThat(jobs.all(predicate)).isTrue()
  }
}
