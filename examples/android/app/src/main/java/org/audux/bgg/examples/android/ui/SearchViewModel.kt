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
package org.audux.bgg.examples.android.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.audux.bgg.BggClient
import org.audux.bgg.common.ThingType
import org.audux.bgg.examples.android.R
import org.audux.bgg.request.collection
import org.audux.bgg.response.Status

class SearchViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(SearchUiState())
  val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

  /** Perform the actual API call with the given query/username. */
  fun search(query: String) {
    // Set the Client logger to VERBOSE.
    BggClient.setLoggerSeverity(BggClient.Companion.Severity.Verbose)

    // Updates the UI state to include the query and indicate a search is taking place.
    _uiState.update { _ -> SearchUiState(query = query, isSearching = true) }

    // Create a new client and use it as an [AutoCloseable]
    viewModelScope.launch {
      val response =
          BggClient().use { client ->
            // Actual API call happens here, trims the username and searches only for board games
            // in the
            // user's collection.
            client.collection(query.trim(), subType = ThingType.BOARD_GAME).call()
          }

      // If the response was successful update the UIState with the collection items / search
      // results.
      if (response.isSuccess()) {
        response.data?.let {
          _uiState.update { currentState ->
            currentState.copy(results = it.items, isSearching = false)
          }
        }
      } else {
        // If the search was not successful for whatever reason update the UIState with the
        // erroneous response (XML/HTML depending on the endpoint used).
        _uiState.update { currentState ->
          currentState.copy(isSearching = false, error = response.error)
        }
      }
    }
  }

  companion object {
    /** Format the URL to open the game page. */
    fun objectIdToWeblink(objectId: Number) = "https://boardgamegeek.com/boardgame/$objectId"

    /**
     * Returns a [@StringRes] representing the collection item's status i.e. "Own" or "For trade"
     * etc.
     */
    @StringRes
    fun statusToStringResource(collectionItemStatus: Status): Int {
      return if (collectionItemStatus.forTrade) {
        R.string.collection_item_for_trade
      } else if (collectionItemStatus.own) {
        R.string.collection_item_own
      } else if (collectionItemStatus.preOrdered) {
        R.string.collection_item_pre_ordered
      } else if (collectionItemStatus.previouslyOwned) {
        R.string.collection_item_previously_owned
      } else if (collectionItemStatus.want) {
        R.string.collection_item_want
      } else if (collectionItemStatus.wantToBuy) {
        R.string.collection_item_want_to_buy
      } else if (collectionItemStatus.wantToPlay) {
        R.string.collection_item_want_to_play
      } else if (collectionItemStatus.wishlist) {
        R.string.collection_item_wishlist
      } else {
        R.string.collection_item_unknown
      }
    }
  }
}
