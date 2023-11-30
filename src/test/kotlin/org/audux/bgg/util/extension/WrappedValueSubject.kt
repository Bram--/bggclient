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
package org.audux.bgg.util.extension

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.audux.bgg.data.response.WrappedLocalDateTime
import org.audux.bgg.data.response.WrappedValue
import java.time.LocalDateTime

/** Truth [Subject] that adds `hasValue()` for [WrappedValue] objects. */
class WrappedValueSubject(failureMetadata: FailureMetadata, private val actual: WrappedValue<*>?) :
    Subject(failureMetadata, actual) {

  fun hasValue(value: Any?) {
    check("value()").that(actual?.value).isEqualTo(value)
  }

  companion object {
    @JvmStatic
    fun assertThat(wrappedValue: WrappedValue<*>?): WrappedValueSubject {
      return Truth.assertAbout(wrappedValues()).that(wrappedValue)
    }

    private fun wrappedValues(): Factory<WrappedValueSubject, WrappedValue<*>> {
      return Factory { a, b -> WrappedValueSubject(a, b) }
    }
  }
}

/** Truth [Subject] that adds `hasValue` for [WrappedLocalDateTime] objects. */
class WrappedLocalDateTimeSubject(
    failureMetadata: FailureMetadata,
    private val actual: WrappedLocalDateTime?
) : Subject(failureMetadata, actual) {

  fun hasValue(value: LocalDateTime?) {
    check("value()").that(actual?.value).isEqualTo(value)
  }

  companion object {
    @JvmStatic
    fun assertThat(wrappedValue: WrappedLocalDateTime): WrappedLocalDateTimeSubject {
      return Truth.assertAbout(wrappedLocalDateTimes()).that(wrappedValue)
    }

    private fun wrappedLocalDateTimes():
        Factory<WrappedLocalDateTimeSubject, WrappedLocalDateTime> {
      return Factory { a, b -> WrappedLocalDateTimeSubject(a, b) }
    }
  }
}
