package com.example.kartar.view.screen.playKarta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.R
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.RoomCreateViewModel
import com.example.kartar.controller.RoomListViewModel
import com.example.kartar.theme.DarkRed
import com.example.kartar.theme.Grey
import com.example.myapplication.view.widget.AppBar
import com.example.myapplication.view.widget.SimpleEditField
import com.example.kartar.view.widget.button.ButtonContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    roomListViewModel: RoomListViewModel,
    roomCreateViewModel: RoomCreateViewModel
) {
    Scaffold(
        topBar = { AppBar(navController, profileViewModel) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RowButtons(navController = navController, roomCreateViewModel = roomCreateViewModel, roomListViewModel = roomListViewModel)
                Spacer(modifier = Modifier.height(30.dp))
                RoomSearchBox(roomListViewModel = roomListViewModel)
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "部屋一覧")
                RoomListColumn(roomListViewModel = roomListViewModel, navController, roomCreateViewModel)
                Spacer(modifier = Modifier.height(60.dp))
                SoloPlayButton(navController = navController)
            }
        }
    }
}

@Composable
private fun RowButtons(navController: NavController, roomCreateViewModel: RoomCreateViewModel, roomListViewModel: RoomListViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        Spacer(modifier = Modifier.height(20.dp))
        RoomCreateButton(navController = navController)
        Spacer(modifier = Modifier.width(30.dp))
        RandomEnterRoomButton(navController = navController, roomCreateViewModel = roomCreateViewModel, roomListViewModel = roomListViewModel)
    }
}

@Composable
private fun RoomCreateButton(navController: NavController) {
    ButtonContent(
        modifier = Modifier
            .height(75.dp)
            .width(145.dp),
        onClick = { navController.navigate("roomCreate") },
        text = "部屋作成",
        fontSize = 18,
        border = 6
    )
}

@Composable
private fun RandomEnterRoomButton(roomListViewModel: RoomListViewModel, roomCreateViewModel: RoomCreateViewModel, navController: NavController) {
    val context = LocalContext.current
    ButtonContent(
        modifier = Modifier
            .height(75.dp)
            .width(145.dp),
        onClick = { roomListViewModel.randomEnterRoom(context = context, roomCreateViewModel = roomCreateViewModel, navController = navController) },
        text = "ランダム\n入出",
        fontSize = 18,
        border = 6
    )
}

@Composable
fun RoomSearchBox(roomListViewModel: RoomListViewModel) {
    SimpleEditField(
        value = roomListViewModel.searchKeyword.value,
        placeholder = "1～20文字で入力してください",
        onClick = { newValue -> roomListViewModel.onSearchBoxChange(newValue) },
        isError = false,
        label = "部屋の検索"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListColumn(roomListViewModel: RoomListViewModel, navController: NavController, roomCreateViewModel: RoomCreateViewModel){
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            .height(220.dp)
    ) {
        items(roomListViewModel.roomList.value.size) { index ->
            Surface(
                onClick = { roomListViewModel.enterRoom(index, navController, roomCreateViewModel = roomCreateViewModel, context = context) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkRed.copy(alpha = 0.05f), shape = RoundedCornerShape(5.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 20.dp,
                                bottom = 20.dp,
                                start = 20.dp,
                                end = 20.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = roomListViewModel.roomList.value[index].name,
                            color = Grey,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${roomListViewModel.roomList.value[index].count}/4",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SoloPlayButton(navController: NavController) {
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(240.dp),
        onClick = { navController.navigate("soloSetup") },
        text = "ひとりで遊ぶ",
        fontSize = 18,
        border = 6
    )
}

@Preview
@Composable
fun RoomListPreview() {
    RoomListScreen(
        navController = rememberNavController(),
        profileViewModel = ProfileViewModel(LocalContext.current, navController = rememberNavController()),
        roomListViewModel = RoomListViewModel(context = LocalContext.current),
        roomCreateViewModel = RoomCreateViewModel(LocalContext.current)
    )
}