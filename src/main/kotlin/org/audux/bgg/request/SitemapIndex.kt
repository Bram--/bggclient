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
package org.audux.bgg.request

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import org.audux.bgg.BggClient.InternalBggClient
import org.audux.bgg.common.Constants
import org.audux.bgg.common.Domain
import org.audux.bgg.response.Response
import org.audux.bgg.response.SitemapIndex

/** Requests the Sitemap index for the given Domain. */
internal fun InternalBggClient.sitemapIndex(domain: Domain) =
    DiffusingSitemap(this) {
        client()
            .get(domain.address) { url { appendPathSegments(Constants.PATH_SITEMAP) } }
            .let { Response.from<SitemapIndex>(it.bodyAsText(), mapper) }
    }
