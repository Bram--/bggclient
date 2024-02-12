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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.audux.bgg.examples.android.R
import org.audux.bgg.examples.android.ui.theme.BggOrange
import org.audux.bgg.response.CollectionItem

/**
 * Encapsulates the entire search screen, containing a [SearchBox] and a list of [ItemRow] when
 * there is a search result.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(searchViewModel: SearchViewModel = viewModel()) {
  val searchUiState by searchViewModel.uiState.collectAsState()
  val uriHandler = LocalUriHandler.current

  Column(modifier = Modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.padding(8.dp).wrapContentSize()) {
      stickyHeader { SearchBox(searchUiState) { query -> searchViewModel.search(query) } }

      if (searchUiState.results.isNotEmpty()) {
        // Display search results
        items(items = searchUiState.results, key = { it.collectionId }) {
          ItemRow(
              item = it,
              onClick = { uriHandler.openUri(SearchViewModel.objectIdToWeblink(it.objectId)) })
        }
      }
    }
    if (searchUiState.isSearching) {
      // Currently searching, show the [SearchingPlaceholder]
      SearchingPlaceholder(modifier = Modifier.fillMaxSize(), query = searchUiState.query)
    } else if (!searchUiState.error.isNullOrBlank()) {
      // Whoops something went wrong!
      ErroneousResponsePlaceholder(
          modifier = Modifier.fillMaxSize(),
          query = searchUiState.query,
          error = searchUiState.error)
    } else if (searchUiState.results.isEmpty()) {
      // Not searching and no search results, show [EmptyStatePlaceholder]
      EmptyStatePlaceholder(modifier = Modifier.fillMaxSize())
    }
  }
}

/**
 * [OutlinedTextField] used for searching the user's collection. Runs [onSearch] whenever `enter` or
 * the search key is pressed/tapped.
 *
 * <p>Also shows an indeterminate [LinearProgressIndicator] whenever there's a search in progress.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBox(
    searchUiState: SearchUiState,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
  var text by rememberSaveable { mutableStateOf("") }
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  val wrappedSearch: (String) -> Unit = { query ->
    focusManager.clearFocus(force = true)
    keyboardController?.hide()
    onSearch(query)
  }

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .wrapContentHeight()
              .background(MaterialTheme.colorScheme.background)) {
        OutlinedTextField(
            text,
            modifier =
                modifier.fillMaxWidth().onKeyEvent {
                  if (it.key == Key.Enter) {
                    wrappedSearch(text)
                    return@onKeyEvent true
                  }
                  return@onKeyEvent false
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { wrappedSearch(text) }),
            onValueChange = { text = it },
            placeholder = {
              ProvideTextStyle(value = TextStyle(Color.Gray)) {
                Text(stringResource(id = R.string.search_box_placeholder))
              }
            },
            leadingIcon = {
              Icon(Icons.TwoTone.Face, contentDescription = null, tint = Color.Gray)
            },
            trailingIcon = {
              if (text.isNotBlank()) {
                TextButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    onClick = {
                      text = ""
                      focusManager.moveFocus(FocusDirection.Previous)
                    }) {
                      Icon(
                          Icons.TwoTone.Close,
                          stringResource(id = R.string.search_box_content_description))
                    }
              }
            })

        if (searchUiState.isSearching) {
          LinearProgressIndicator(
              modifier =
                  Modifier.height(4.dp)
                      .padding(1.dp)
                      .fillMaxWidth()
                      .align(Alignment.BottomStart)
                      .clip(RoundedCornerShape(0.dp, 0.dp, 2.dp, 2.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.background)
        }
      }
}

/**
 * Row for a single collection item, it shows an image, name, comment and 'status' e.g. own, want to
 * buy etc.
 */
@Composable
fun ItemRow(item: CollectionItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Row(
      modifier =
          modifier.padding(horizontal = 8.dp, vertical = 8.dp).height(IntrinsicSize.Min).clickable {
            onClick()
          }) {
        AsyncImage(
            model = item.image,
            contentDescription = null,
            modifier = Modifier.heightIn(min = 80.dp).width(80.dp),
            contentScale = ContentScale.FillWidth)

        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp)) {
          ProvideTextStyle(value = TextStyle(fontWeight = FontWeight.Bold)) {
            Text(text = item.name)
          }

          item.status?.let { status ->
            ProvideTextStyle(value = TextStyle(fontSize = 11.sp)) {
              Text(
                  text =
                      stringResource(
                          R.string.collection_item_status,
                          stringResource(id = SearchViewModel.statusToStringResource(status))),
                  modifier = Modifier.wrapContentSize().background(BggOrange))
            }
          }

          item.comment?.let { comment ->
            ProvideTextStyle(value = TextStyle(fontSize = 11.sp)) {
              Text(text = comment, modifier = Modifier.padding(top = 4.dp))
            }
          }
        }
      }
}

/** Shown when no searches have taken place yet. */
@Composable
fun EmptyStatePlaceholder(modifier: Modifier) {
  val logoSize = 200.dp

  Box(modifier = modifier.padding(horizontal = 16.dp)) {
    Image(
        painter = painterResource(R.drawable.boardgamegeek_logo),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceVariant),
        modifier =
            Modifier.focusable(false)
                .size(logoSize)
                .align(Alignment.Center)
                .offset(y = logoSize / 2 * -1))

    ProvideTextStyle(value = TextStyle(MaterialTheme.colorScheme.surfaceVariant)) {
      Text(
          text = stringResource(R.string.search_placeholder_do_a_search),
          textAlign = TextAlign.Center,
          modifier = Modifier.align(Alignment.Center).offset(y = logoSize / 4).fillMaxWidth())
    }
  }
}

/** Shown when a search is in progress.. */
@Composable
fun SearchingPlaceholder(modifier: Modifier, query: String?) {
  val logoSize = 200.dp

  Box(modifier = modifier.padding(horizontal = 16.dp)) {
    Icon(
        imageVector = Icons.TwoTone.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            Modifier.focusable(false)
                .size(logoSize)
                .align(Alignment.Center)
                .offset(y = logoSize / 2 * -1))

    ProvideTextStyle(value = TextStyle(MaterialTheme.colorScheme.surfaceVariant)) {
      Text(
          text = stringResource(R.string.search_placeholder_searching, query ?: ""),
          textAlign = TextAlign.Center,
          modifier = Modifier.align(Alignment.Center).offset(y = logoSize / 4).fillMaxWidth())
    }
  }
}

/** Shown when a search is in progress.. */
@Composable
fun ErroneousResponsePlaceholder(modifier: Modifier, query: String?, error: String?) {
  val logoSize = 200.dp

  Box(modifier = modifier.padding(horizontal = 16.dp)) {
    Icon(
        imageVector = Icons.TwoTone.Warning,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            Modifier.focusable(false)
                .size(logoSize)
                .align(Alignment.Center)
                .offset(y = logoSize / 2 * -1))

    Column(
        modifier =
            Modifier.align(Alignment.Center)
                .wrapContentHeight()
                .fillMaxWidth()
                .offset(y = logoSize / 2)) {
          ProvideTextStyle(value = TextStyle(MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                text = stringResource(R.string.search_placeholder_error, query ?: ""),
                textAlign = TextAlign.Center)
          }
          ProvideTextStyle(
              value = TextStyle(MaterialTheme.colorScheme.surfaceVariant, fontSize = 11.sp)) {
                Text(
                    text = stringResource(R.string.search_placeholder_error_response, error ?: ""),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight())
              }
        }
  }
}
