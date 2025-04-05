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
package org.audux.bgg.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import java.io.InputStream
import kotlinx.coroutines.delay
import org.audux.bgg.BggClient
import org.audux.bgg.InstantiableClient

object TestUtils {
    val DEFAULT_HEADERS =
        Headers.build {
            appendAll("Accept-Encoding", listOf("gzip"))
            appendAll("Accept-Charset", listOf("UTF-8"))
            appendAll("Accept", listOf("*/*"))
        }

    /**
     * Sets up a HttpEngine using a [MockEngine] and [respondOk] responses with the given xml files
     * as the actual response.
     */
    @JvmStatic
    @JvmOverloads
    fun setupMockEngine(vararg xmlFileName: String, headers: Headers = Headers.Empty) =
        MockEngine(
            MockEngineConfig().apply {
                xmlFileName.map { fileName ->
                    addHandler { respond(xml(fileName).readAllBytes(), headers = headers) }
                }
            }
        )

    /** Returns an fully configure [XmlMapper] instance that is used in the BggClient. */
    @JvmStatic fun getBggClientMapper() = InstantiableClient().mapper

    /** Returns input stream of `resources/xml/{fileName}.xml` to use in testing. */
    @JvmStatic
    fun xml(fileName: String): InputStream {
        try {
            return TestUtils::class.java.classLoader.getResourceAsStream("xml/$fileName.xml")!!
        } catch (e: NullPointerException) {
            throw IllegalArgumentException("Could not find xml/$fileName.xml", e)
        }
    }

    /** Sets up [TestLogWriter] for the Logger so assertions can be made. */
    @JvmStatic
    @JvmOverloads
    fun captureLoggerWrites(severity: BggClient.Severity = BggClient.Severity.Verbose) =
        TestLogWriter().also {
            Logger.setLogWriters(it)
            BggClient.setLoggerSeverity(severity)
        }

    /** LogWriter for test assertions. */
    class TestLogWriter : LogWriter() {
        private val logWrites: MutableList<LogWrite> = mutableListOf()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            logWrites.add(LogWrite(severity, message, tag, throwable))
        }

        fun logsWritten() = logWrites.toList()
    }

    fun delayedResponse(
        delayMs: Long = 20
    ): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        delay(delayMs)
        respondOk("OK")
    }

    fun instantResponse(): MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respondOk("OK")
    }

    /** All data that is logged in a single [Logger.log] call. */
    data class LogWrite(
        val severity: Severity,
        val message: String,
        val tag: String,
        val throwable: Throwable?,
    )
}
