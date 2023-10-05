package com.example.kartar.view.screen.create.server

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.kartar.R
import com.example.kartar.controller.CreateViewModel
import com.example.kartar.controller.KartaSearchViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.theme.LiteGreen
import com.example.kartar.view.screen.create.SettingText
import com.example.myapplication.view.widget.AppBar
import com.example.kartar.view.widget.button.ButtonContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerKartaDetail(
    createViewModel: CreateViewModel,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    kartaSearchViewModel: KartaSearchViewModel,
    kartaUid: String
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
                KartaInformationText(kartaSearchViewModel = kartaSearchViewModel, kartaUid = kartaUid)
                Spacer(modifier = Modifier.height(30.dp))
                ServerKartaDetailRow(createViewModel = createViewModel, kartaUid = kartaUid, kartaSearchViewModel = kartaSearchViewModel)
                Spacer(modifier = Modifier.height(60.dp))
                DownloadServerKarta(kartaSearchViewModel = kartaSearchViewModel)
            }
        }
        //ダウンロード前に表示するダイアログ
        if (kartaSearchViewModel.showDownloadDialog.value) {
            KartaDownloadDialog(kartaSearchViewModel = kartaSearchViewModel, kartaUid = kartaUid, navController)
        }
    }
    //サーバ処理中に表示
    if (kartaSearchViewModel.showIndicator.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = LiteGreen)
        }
    }
}

@Composable
fun KartaInformationText(kartaSearchViewModel: KartaSearchViewModel, kartaUid: String) {
    kartaSearchViewModel.getKartaInformation(kartaUid)
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = kartaSearchViewModel.kartaTitle.value,
            fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
            fontSize = 18.sp,
            color = Grey
        )
        Text(
            text = kartaSearchViewModel.kartaGenre.value,
            fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
            fontSize = 16.sp,
            color = Grey2
        )
        Text(
            text = kartaSearchViewModel.kartaDescription.value,
            fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
            fontSize = 16.sp,
            color = Grey2
        )
    }
}

@Composable
fun ServerKartaDetailRow(createViewModel: CreateViewModel, kartaUid: String, kartaSearchViewModel: KartaSearchViewModel) {
    kartaSearchViewModel.getYomifudaAndEfuda(kartaUid)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(kartaSearchViewModel.kartaDataList.value.size) { index ->
            Column(
                modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingText(text = "絵札")
                ServerKartaImage(createViewModel = createViewModel, uri = kartaSearchViewModel.kartaDataList.value[index].efuda, index = index)
                Spacer(modifier = Modifier.height(20.dp))
                SettingText(text = "読み札")
                Text(
                    text = kartaSearchViewModel.kartaDataList.value[index].yomifuda,
                    fontFamily = FontFamily(Font(R.font.kiwimaru_regular)),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ServerKartaImage(createViewModel: CreateViewModel, uri: String, index: Int) {
    val painter = rememberAsyncImagePainter(model = uri)
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
                painter = painter,
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
fun DownloadServerKarta(kartaSearchViewModel: KartaSearchViewModel) {
    ButtonContent(
        modifier = Modifier
            .height(50.dp)
            .width(250.dp),
        onClick = { kartaSearchViewModel.showDownloadDialog.value = true },
        text = "ダウンロードする",
        border = 4,
        fontSize = 18,
        fontWeight = FontWeight.Normal,
        borderColor = ButtonBorder
    )
}

@Composable
fun KartaDownloadDialog(kartaSearchViewModel: KartaSearchViewModel, kartaUid: String, navController: NavController) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { kartaSearchViewModel.showDownloadDialog.value = false },
        title = { Text(text = "最終確認", color = DarkGreen)},
        text = {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "このかるたをダウンロードしてもよろしいですか？", color = Grey)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                kartaSearchViewModel.showDownloadDialog.value = false
            }) {
                Text(text = "NO", color = Grey2, fontSize = 16.sp)
            }
            TextButton(
                onClick = { kartaSearchViewModel.downloadKarta(context = context, kartaUid = kartaUid, navController = navController)}
            ) {
                Text(text = "OK", color = DarkGreen, fontSize = 16.sp)
            }
        }
    )
}