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
import io.ktor.client.engine.mock.respondOk
import java.io.InputStream
import org.audux.bgg.BggClient
import org.audux.bgg.BggClient.InternalBggClient

object TestUtils {
    /**
     * Sets up a HttpEnginecusing a [MockEngine] and [respondOk] responses with the given xml files
     * as the actual response.
     */
    fun setupMockEngine(vararg xmlFileName: String) =
        MockEngine(
            MockEngineConfig().apply {
                xmlFileName.map { fileName ->
                    addHandler { respondOk(String(xml(fileName).readAllBytes())) }
                }
            }
        )

    /** Returns an fully configure [XmlMapper] instance that is used in the BggClient. */
    fun getBggClientMapper() = InternalBggClient().mapper

    /** Returns input stream of `resources/xml/{fileName}.xml` to use in testing. */
    fun xml(fileName: String): InputStream {
        try {
            return TestUtils::class.java.classLoader.getResourceAsStream("xml/$fileName.xml")!!
        } catch (e: NullPointerException) {
            throw IllegalArgumentException("Could not find xml/$fileName.xml", e)
        }
    }

    /** Sets up [TestLogWriter] for the Logger so assertions can be made. */
    internal fun captureLoggerWrites(severity: BggClient.Severity = BggClient.Severity.Verbose) =
        TestLogWriter().also {
            Logger.setLogWriters(it)
            BggClient.setLoggerSeverity(severity)
        }

    /** LogWriter for test assertions. */
    internal class TestLogWriter : LogWriter() {
        private val logWrites: MutableList<LogWrite> = mutableListOf()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            logWrites.add(LogWrite(severity, message, tag, throwable))
        }

        fun logsWritten() = logWrites.toList()
    }

    /** All data that is logged in a single [Logger.log] call. */
    data class LogWrite(
        val severity: Severity,
        val message: String,
        val tag: String,
        val throwable: Throwable?
    )
}
