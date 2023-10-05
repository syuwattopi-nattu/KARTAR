package com.example.kartar.view.screen.playKarta

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.R
import com.example.kartar.controller.RoomCreateViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.DarkRed
import com.example.kartar.theme.Grey
import com.example.myapplication.view.widget.PlayerIcon
import com.example.kartar.view.widget.button.ButtonContent

@Composable
fun StandByRoomScreen(
    navController: NavController,
    roomCreateViewModel: RoomCreateViewModel
) {
    BackHandler {
        roomCreateViewModel.exitRoom(navController)
    }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WaitingNumberText(roomCreateViewModel = roomCreateViewModel)
            Spacer(modifier = Modifier.height(20.dp))
            PlayerColumn(roomCreateViewModel = roomCreateViewModel)
            if (roomCreateViewModel.ownerUid.value == FirebaseSingleton.currentUid()) {
                GameStartButton(roomCreateViewModel = roomCreateViewModel)
            }
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.04f))
            ExitRoomButton(navController = navController, roomCreateViewModel = roomCreateViewModel)
        }
    }
}

@Composable
fun WaitingNumberText(roomCreateViewModel: RoomCreateViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,) {
        Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.02f))
        Text(
            text = roomCreateViewModel.enterRoomName.value,
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.kiwimaru_medium))
        )
        Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.02f))
        Text(
            text = "待機人数",
            color = Grey,
            fontSize = 16.sp
        )
        Text(text = "${roomCreateViewModel.standByPlayer.value}/4")
    }
}

@Composable
fun PlayerColumn(roomCreateViewModel: RoomCreateViewModel) {
    LazyColumn(
        modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            .height(220.dp)
    ) {
        items(roomCreateViewModel.playerInformation.value.size) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkRed.copy(alpha = 0.05f), shape = RoundedCornerShape(5.dp))
            ) {
                Row {
                    PlayerIcon(
                        modifier = Modifier.size(30.dp),
                        borderWidth = 1,
                        model = roomCreateViewModel.playerInformation.value[index].second
                    )
                    Text(text = roomCreateViewModel.playerInformation.value[index].first)
                    if (roomCreateViewModel.allPlayers.value.size > index) {
                        Text(text = roomCreateViewModel.allPlayers.value[index].state)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExitRoomButton(navController: NavController, roomCreateViewModel: RoomCreateViewModel) {
    val text = if (roomCreateViewModel.ownerUid.value == FirebaseSingleton.currentUid()) {
        "解散する"
    } else "退室する"
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = { roomCreateViewModel.exitRoom(navController = navController) },
        text = text,
        border = 4,
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal,
        borderColor = DarkRed
    )
}

@Composable
private fun GameStartButton(roomCreateViewModel: RoomCreateViewModel) {
    val context = LocalContext.current
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = { roomCreateViewModel.gameStart(context) },
        text = "ゲーム開始する",
        border = 4,
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal,
        borderColor = ButtonBorder
    )
}

@Preview
@Composable
private fun StandByRoomPreview() {
    StandByRoomScreen(navController = rememberNavController(), roomCreateViewModel = viewModel())
}