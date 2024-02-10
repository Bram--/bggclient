package org.audux.bgg.examples.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.audux.bgg.BggClient
import org.audux.bgg.examples.android.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AndroidTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column(
              modifier =
                  Modifier.statusBarsPadding()
                      .verticalScroll(rememberScrollState())
                      .safeDrawingPadding()
                      .padding(Dp(8f)),
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                SearchBox()
              }
        }
      }
    }
  }
}

@Composable
fun SearchBox(modifier: Modifier = Modifier) {
  var text by rememberSaveable { mutableStateOf("") }
  Row(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
    TextField(
        text,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { text = it },
        label = { ProvideTextStyle(value = TextStyle(Color.Gray)) { Text("username") } },
        leadingIcon = { Icon(Icons.TwoTone.Face, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            TextButton(
                modifier = Modifier.padding(horizontal =  Dp(8f)),
                onClick = {
                    BggClient.setLoggerSeverity(BggClient.Companion.Severity.Verbose)
                    //        BggClient().user("Novaeux").callAsync {
                    //            Log.e("TAG", "USer: $it")
                    //        }
                }) {
                Icon(Icons.TwoTone.Search, "Search")
            }

        }
    )
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  AndroidTheme { SearchBox() }
}
