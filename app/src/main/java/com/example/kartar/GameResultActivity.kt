package com.example.kartar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.kartar.controller.GameResultViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.DarkRed
import com.example.kartar.view.widget.button.ButtonContent
import com.example.myapplication.view.widget.PlayerIcon

class GameResultActivity : ComponentActivity(){
    private val gameResultViewModel = GameResultViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameResultViewModel.roomUid.value = intent.getStringExtra("ROOM_UID").toString()
        Log.d("uid", intent.getStringExtra("ROOM_UID").toString())
        Log.d("uid", gameResultViewModel.roomUid.value)
        gameResultViewModel.getRankingData(this)

        setContent {
            ResultScreen()
        }
    }

    @Composable
    fun ResultScreen() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ゲームの結果")
                    Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.01f))
                    RankingColumn()
                    Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.3f))
                    ResultCheckedButton()
                }
            }
        }
    }

    @Composable
    private fun RankingColumn() {
        LazyColumn {
            items(gameResultViewModel.rankingList.value.size) {index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkRed.copy(alpha = 0.05f), shape = RoundedCornerShape(5.dp))
                ) {
                    Row {
                        Text(text = "${index + 1}位")
                        PlayerIcon(
                            modifier = Modifier.size(60.dp),
                            borderWidth = 1,
                            model = gameResultViewModel.rankingList.value[index].iconUri.toString()
                        )
                        Text(text = gameResultViewModel.rankingList.value[index].name)
                        Text(text = "${gameResultViewModel.rankingList.value[index].point}点")
                    }
                }
            }
        }
    }

    @Composable
    private fun ResultCheckedButton() {
        val context = LocalContext.current
        ButtonContent(
            onClick = { gameResultViewModel.onResultCheckedButtonClick(context = context) },
            text = "終了する",
            modifier = ConstantsSingleton.widthButtonModifier,
            fontSize = ConstantsSingleton.widthButtonText,
            border = 4,
            fontWeight = FontWeight.Normal
        )
    }

    data class RankingData(
        var name: String = "",
        var iconUri: Uri = "".toUri(),
        var point: Int = 0,
    )

    @Preview
    @Composable
    private fun ResultScreenPreview() {
        ResultScreen()
    }
}