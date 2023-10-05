package com.example.kartar.view.screen.playKarta

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.myapplication.view.widget.AppBar
import com.example.myapplication.view.widget.CircleYomifudaText
import com.example.myapplication.view.widget.SimpleEditField
import com.example.kartar.view.widget.button.ButtonContent
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCreateScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
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
                TopComponent(roomCreateViewModel = roomCreateViewModel)
                //RoomPasswordEditField(roomCreateViewModel = roomCreateViewModel)
                KartaSelectComponent(roomCreateViewModel = roomCreateViewModel)
                PlaySelectComponent(roomCreateViewModel = roomCreateViewModel)
                BottomComponent(roomCreateViewModel = roomCreateViewModel, navController = navController)
            }
        }
    }
}

@Composable
private fun TopComponent(roomCreateViewModel: RoomCreateViewModel) {
    Box(
        modifier = Modifier.fillMaxHeight(fraction = 0.18f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PublicOrPrivate(roomCreateViewModel = roomCreateViewModel)
            RoomNameEditField(roomCreateViewModel = roomCreateViewModel)
        }
    }
}

@Composable
private fun KartaSelectComponent(roomCreateViewModel: RoomCreateViewModel) {
    Box(
        modifier = Modifier.fillMaxHeight(fraction = 0.6f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayKartaText(roomCreateViewModel = roomCreateViewModel)
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.02f))
            Row(modifier = Modifier.height(180.dp)) {
                SelectAKarta(roomCreateViewModel = roomCreateViewModel)
                Spacer(modifier = Modifier.width(10.dp))
                SelectionColum(roomCreateViewModel = roomCreateViewModel)
            }
        }
    }
}

@Composable
private fun PlaySelectComponent(roomCreateViewModel: RoomCreateViewModel) {
    Box(
        modifier = Modifier.fillMaxHeight(fraction = 0.3f),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "プレイ回数", color = Color.Gray)
            Spacer(modifier = Modifier.fillMaxWidth(fraction = 0.05f))
            ChoiceComponent(roomCreateViewModel = roomCreateViewModel)
        }
    }
}

@Composable
private fun BottomComponent(roomCreateViewModel: RoomCreateViewModel, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxHeight(fraction = 0.7f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RoomCreateButton(roomCreateViewModel = roomCreateViewModel, navController)
        }
    }
}

@Composable
fun PublicOrPrivate(roomCreateViewModel: RoomCreateViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (roomCreateViewModel.roomIsPublic.value) {
            Text(
                text = "誰とでも対戦",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Grey
            )
        } else {
            Text(
                text = "プライベート対戦",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Grey
            )
        }
        IconButton(
            onClick = { roomCreateViewModel.roomIsPublic.value  = !roomCreateViewModel.roomIsPublic.value },
            modifier = Modifier
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = Icons.Default.Cached,
                contentDescription = null,
                tint = DarkGreen,
            )
        }
    }
}

@Composable
private fun RoomNameEditField(roomCreateViewModel: RoomCreateViewModel) {
    SimpleEditField(
        value = roomCreateViewModel.roomName.value,
        onClick = { newString -> roomCreateViewModel.onRoomNameChange(newString) },
        isError = roomCreateViewModel.isRoomNameValid.value,
        label = "部屋の名前",
        placeholder = "1～15文字で設定してください"
    )
}

/*
@Composable
private fun RoomPasswordEditField(roomCreateViewModel: RoomCreateViewModel) {
    if (!roomCreateViewModel.roomIsPublic.value) {
        SimpleEditField(
            value = roomCreateViewModel.roomPassword.value,
            onClick = { newString -> roomCreateViewModel.onRoomPasswordChange(newString) },
            isError = roomCreateViewModel.isRoomPasswordValid.value,
            label = "パスワード",
            placeholder = "5～15文字の英数字設定してください"
        )
    }
}

 */

@Composable
fun ChoiceComponent(
    options: List<Int> = listOf(1, 5, 10, 20),
    roomCreateViewModel: RoomCreateViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ClickableText(
            text = AnnotatedString(roomCreateViewModel.selectedOption.value.toString()),
            onClick = { expanded = true },
            modifier = Modifier.padding(16.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.toString()) },
                    onClick = {
                        roomCreateViewModel.selectedOption.intValue = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PlayKartaText(roomCreateViewModel: RoomCreateViewModel) {
    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = roomCreateViewModel.playKartaTitle.value,
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
fun SelectionColum(roomCreateViewModel: RoomCreateViewModel) {
    val context = LocalContext.current

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(roomCreateViewModel.kartaDirectories.value.size) { index ->
            val sharedPref = context.getSharedPreferences(roomCreateViewModel.kartaDirectories.value[index].name, Context.MODE_PRIVATE)
            val kartaName = sharedPref.getString("title", "")

            Surface(
                onClick = {
                    roomCreateViewModel.playKartaUid.value = roomCreateViewModel.kartaDirectories.value[index].name
                    roomCreateViewModel.playKartaTitle.value = kartaName.toString()
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
fun SelectAKarta(roomCreateViewModel: RoomCreateViewModel) {
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
            SelectKartaImage(roomCreateViewModel = roomCreateViewModel)
        }
        CircleYomifudaText(
            modifier = Modifier.align(Alignment.TopEnd),
            text = "あ"
        )
    }
}

@Composable
fun SelectKartaImage(roomCreateViewModel: RoomCreateViewModel) {
    if (roomCreateViewModel.playKartaUid.value != "") { //かるたを選択している場合
        val context = LocalContext.current
        val imageFile = File(context.filesDir, "karta/${roomCreateViewModel.playKartaUid.value}/0.png")
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

@Composable
private fun RoomCreateButton(roomCreateViewModel: RoomCreateViewModel, navController: NavController) {
    val context = LocalContext.current
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(240.dp),
        onClick = { roomCreateViewModel.gameRoomCreate(context = context, navController = navController) },
        text = "部屋を作成",
        fontSize = 18,
        border = 6
    )
}

@Preview
@Composable
private fun RoomCreateScreenPreview() {
    RoomCreateScreen(
        navController = rememberNavController(),
        profileViewModel = ProfileViewModel(context = LocalContext.current, navController = rememberNavController()),
        roomCreateViewModel = RoomCreateViewModel(context = LocalContext.current)
    )
}