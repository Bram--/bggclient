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
package org.audux.bgg.util

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import java.io.InputStream
import org.audux.bgg.BggClient
import org.audux.bgg.module.BggHttpEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module

object TestUtils {
    /** Returns input stream of `resources/xml/{fileName}.xml` to use in testing. */
    fun xml(fileName: String): InputStream {
        return TestUtils::class.java.classLoader.getResourceAsStream("xml/$fileName.xml")!!
    }

    fun setupEngineAndRequest(xmlFileName: String): BggClient {
        return BggClient().apply {
            val mockEngine = MockEngine { respondOk(String(xml(xmlFileName).readAllBytes())) }

            getKoin()
                .loadModules(
                    listOf(
                        module {
                            single(named<BggHttpEngine>()) {
                                // Not useless as mockEngine needs to be bound to
                                // HttpClientEngine
                                // and not set up a new binding for HttpClientEngine
                                @Suppress("USELESS_CAST")
                                mockEngine as HttpClientEngine
                            }
                        }
                    )
                )
        }
    }
}
