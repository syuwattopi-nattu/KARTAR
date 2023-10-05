package com.example.kartar.view.screen.create

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import coil.compose.rememberAsyncImagePainter
import com.example.kartar.R
import com.example.kartar.controller.CreateViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.DarkRed
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.theme.LiteGreen
import com.example.myapplication.view.widget.AppBar
import com.example.kartar.view.widget.button.ButtonContent
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KartaDetailScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    createViewModel: CreateViewModel,
    kartaUid: String
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
    val kartaState = sharedPref.getString("state", "").toString()
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
                KartaTitleText(kartaUid = kartaUid)
                KartaGenreText(kartaUid = kartaUid)
                KartaDescriptionText(kartaUid = kartaUid)
                Spacer(modifier = Modifier.height(20.dp))
                KartaDetailRow(kartaUid = kartaUid, createViewModel = createViewModel)
                Spacer(modifier = Modifier.height(50.dp))
                if (kartaState == "ローカル") {
                    SaverPopButton(createViewModel = createViewModel, kartaUid = kartaUid)
                } else if (kartaState == "サーバ") {
                    /*TODO:サーバから削除する(めんどいので後で)*/
                }
                Spacer(modifier = Modifier.height(10.dp))
                KartaDeleteButton(createViewModel = createViewModel)
            }
        }
    }
    //サーバ処理中に表示するインディケーター
    if (createViewModel.showProcessIndicator.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = LiteGreen)
        }
    } else if (createViewModel.showKartaDeleteDialog.value) {
        kartaDeleteDialog(
            createViewModel = createViewModel,
            navController = navController,
            kartaUid = kartaUid
        )
    }
}

@Composable
fun KartaTitleText(kartaUid: String) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
    val kartaName = sharedPref.getString("title", "かるたのタイトル").toString()
    Text(
        text = kartaName,
        fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
        fontSize = 18.sp,
        color = Grey
    )
}

@Composable
fun KartaGenreText(kartaUid: String) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
    val kartaDescription = sharedPref.getString("genre", "").toString()
    Text(
        text = "ジャンル：${kartaDescription}",
        fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
        fontSize = 16.sp,
        color = Grey2
    )
}

@Composable
fun KartaDescriptionText(kartaUid: String) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
    val kartaDescription = sharedPref.getString("description", "").toString()
    Text(
        text = kartaDescription,
        fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
        fontSize = 16.sp,
        color = Grey2
    )
}

@Composable
fun KartaDetailRow(kartaUid: String, createViewModel: CreateViewModel) {
    val context = LocalContext.current
    val dir = File(context.filesDir, "karta/$kartaUid")
    val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
    //画像フォルダをすべて取得
    val allFiles = dir.listFiles()
    val imageFiles = allFiles?.filter {
        it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg"))
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(imageFiles?.size ?: 0) { index ->
            val image = File(dir, "$index.png").takeIf { it.exists() }
            val yomifuda = sharedPref.getString(index.toString(), "").toString()
            Column(
                modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingText(text = "絵札")
                if (image != null) {
                    KartaImage(imageFile = image, createViewModel = createViewModel, index = index)
                }
                Spacer(modifier = Modifier.height(20.dp))
                SettingText(text = "読み札")
                Text(
                    text = yomifuda,
                    fontFamily = FontFamily(Font(R.font.kiwimaru_regular)),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun KartaImage(imageFile: File, createViewModel: CreateViewModel, index: Int) {
    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
    val imagePainter = rememberAsyncImagePainter(model = imageBitmap)
    Box {
        Box(
            modifier = Modifier
                .border(
                    width = 8.dp,
                    color = ButtonBorder,
                    shape = RoundedCornerShape(5.dp)
                )
                .background(color = ButtonContainer.copy(alpha = 0.4f))
        ) {
            Image(
                modifier = Modifier
                    .height(253.dp)
                    .width(180.dp),
                painter = imagePainter,
                contentDescription = null
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp, end = 3.dp)
                .size(60.dp)
                .background(Color.White, shape = CircleShape)
                .border(width = 3.dp, color = Grey, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = createViewModel.hiraganaList[index],
                fontSize = 28.sp,
                color = Grey,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.kiwimaru_regular))
            )
        }
    }
}

@Composable
private fun SaverPopButton(createViewModel: CreateViewModel, kartaUid: String) {
    val context = LocalContext.current
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(250.dp),
        onClick = { createViewModel.uploadKarta(context = context, kartaUid) },
        text = "みんなに公開",
        border = 4,
        fontSize = 18,
        fontWeight = FontWeight.Normal
    )
}

@Composable
private fun KartaDeleteButton(createViewModel: CreateViewModel) {
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(250.dp),
        onClick = { createViewModel.showKartaDeleteDialog.value = true },
        text = "かるたを削除",
        border = 4,
        fontSize = 18,
        fontWeight = FontWeight.Normal,
        borderColor = DarkRed
    )
}

@Composable
private fun kartaDeleteDialog(
    createViewModel: CreateViewModel,
    navController: NavController,
    kartaUid: String
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { createViewModel.showKartaDeleteDialog.value = false },
        title = { Text(text = "最終確認", color = Color.Red)},
        text = {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "かるたを削除します", color = Grey)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "よろしいでしょうか？", color = Grey)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                createViewModel.showKartaDeleteDialog.value = false
            }) {
                Text(text = "NO", color = Grey2, fontSize = 16.sp)
            }
            TextButton(
                onClick = { createViewModel.kartaDelete(navController = navController, kartaUid = kartaUid, context = context)}
            ) {
                Text(text = "OK", color = DarkGreen, fontSize = 16.sp)
            }
        }
    )
}

@Preview
@Composable
private fun KartaDetailScreenView() {
    KartaDetailScreen(
        navController = rememberNavController(),
        profileViewModel = ProfileViewModel(LocalContext.current, rememberNavController()),
        kartaUid = "",
        createViewModel = viewModel()
    )
}