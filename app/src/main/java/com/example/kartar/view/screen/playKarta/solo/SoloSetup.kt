package com.example.kartar.view.screen.playKarta.solo

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.R
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.RoomListViewModel
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.myapplication.view.widget.AppBar
import com.example.myapplication.view.widget.CircleYomifudaText
import com.example.kartar.view.widget.button.ButtonContent
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloSetupScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    roomListViewModel: RoomListViewModel
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
                Text(text = "遊ぶかるた", color = Grey2)
                Spacer(modifier = Modifier.height(10.dp))
                PlayKartaText(roomListViewModel = roomListViewModel)
                Spacer(modifier = Modifier.height(30.dp))
                Row {
                    SelectKarta(roomListViewModel = roomListViewModel)
                    Spacer(modifier = Modifier.width(10.dp))
                    KartaSelectionColum(roomListViewModel = roomListViewModel)
                }
                Spacer(modifier = Modifier.height(60.dp))
                PlayStartButton(roomListViewModel)
            }
        }
    }
}

@Composable
private fun PlayKartaText(roomListViewModel: RoomListViewModel) {
    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = roomListViewModel.playKartaTitle.value,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center),
                color = Grey,
                fontFamily = FontFamily(Font(R.font.kiwimaru_medium))
            )
            Divider(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp),
                color = Grey2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KartaSelectionColum(roomListViewModel: RoomListViewModel) {
    val context = LocalContext.current
    roomListViewModel.getKartaInDirectories(context)

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(roomListViewModel.kartaDirectories.value.size) { index ->
            val sharedPref = context.getSharedPreferences(roomListViewModel.kartaDirectories.value[index].name, Context.MODE_PRIVATE)
            val kartaName = sharedPref.getString("title", "")

            Surface(
                onClick = {
                    roomListViewModel.playKartaUid.value = roomListViewModel.kartaDirectories.value[index].name
                    roomListViewModel.playKartaTitle.value = kartaName.toString()
                }
            ) {
                Text(
                    text = kartaName.toString(),
                    fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
                    fontSize = 18.sp,
                    color = Grey,
                )
            }
        }
    }
}

@Composable
private fun PlayStartButton(roomListViewModel: RoomListViewModel) {
    val context = LocalContext.current
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(145.dp),
        onClick = { roomListViewModel.onClickSoloPlayStartButton(context) },
        text = "開始",
        fontSize = 18,
        border = 6
    )
}

@Composable
fun SelectKarta(roomListViewModel: RoomListViewModel) {
    Box(modifier = Modifier.background(Color.White)) {
        Box(
            modifier = Modifier
                .height(210.dp)
                .width(150.dp)
                .border(
                    width = 7.dp,
                    color = ButtonBorder,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            SelectKartaImage(roomListViewModel = roomListViewModel)
        }
        CircleYomifudaText(
            modifier = Modifier.align(Alignment.TopEnd),
            text = "あ"
        )
    }
}

@Composable
fun SelectKartaImage(roomListViewModel: RoomListViewModel) {
    if (roomListViewModel.playKartaUid.value != "") { //かるたを選択している場合
        val context = LocalContext.current
        val imageFile = File(context.filesDir, "karta/${roomListViewModel.playKartaUid.value}/0.png")
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        Box {
            Image(
                modifier = Modifier
                    .width(200.dp)
                    .height(280.dp)
                    .align(Alignment.Center),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview
@Composable
private fun SoloSetupPreview() {
    SoloSetupScreen(
        navController = rememberNavController(),
        profileViewModel = ProfileViewModel(LocalContext.current, navController = rememberNavController()),
        roomListViewModel = RoomListViewModel(context = LocalContext.current)
    )
}