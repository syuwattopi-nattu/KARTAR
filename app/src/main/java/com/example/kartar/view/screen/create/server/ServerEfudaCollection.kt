package com.example.kartar.view.screen.create.server

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.kartar.R
import com.example.kartar.controller.KartaSearchViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.DarkRed
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.myapplication.view.widget.AppBar
import com.example.myapplication.view.widget.SimpleEditField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerEfudaCollectionScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    kartaSearchViewModel: KartaSearchViewModel
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
                ServerKartaSearchBox(kartaSearchViewModel = kartaSearchViewModel)
                ChangeSearchTypeButton(kartaSearchViewModel = kartaSearchViewModel)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "サーバのかるた一覧", color = Grey)
                ShowGetKartaDataColumn(navController, kartaSearchViewModel)
            }
        }
    }
}

@Composable
fun ServerKartaSearchBox(kartaSearchViewModel: KartaSearchViewModel) {
    SimpleEditField(
        value = kartaSearchViewModel.searchKeyword.value,
        placeholder = kartaSearchViewModel.searchBoxPlaceholder.value,
        onClick = { newValue -> kartaSearchViewModel.onClickServerKartaSearchBox(newValue = newValue) },
        isError = false,
        label = kartaSearchViewModel.searchBoxLabel.value
    )
}

@Composable
fun ChangeSearchTypeButton(kartaSearchViewModel: KartaSearchViewModel) {
    IconButton(onClick = { kartaSearchViewModel.changeSearchType() }) {
        Icon(
            modifier = Modifier.size(30.dp),
            imageVector = Icons.Default.Cached,
            contentDescription = null,
            tint = DarkRed.copy(alpha = 0.8f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowGetKartaDataColumn(navController: NavController, kartaSearchViewModel: KartaSearchViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(6.dp)
    ) {
        items(kartaSearchViewModel.kartaDataFromServerList.value.size) { index ->
            Column {
                Surface(
                    onClick = { navController.navigate("serverKartaDetail/${kartaSearchViewModel.kartaDataFromServerList.value[index].kartaUid}") }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                DarkRed.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            KartaImage(kartaSearchViewModel.kartaDataFromServerList.value[index].kartaImage)
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text(
                                    text = kartaSearchViewModel.kartaDataFromServerList.value[index].title,
                                    fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
                                    fontSize = 18.sp,
                                    color = Grey
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ジャンル:${kartaSearchViewModel.kartaDataFromServerList.value[index].genre}",
                                    fontSize = 14.sp,
                                    color = Grey2
                                )
                                Text(
                                    text = kartaSearchViewModel.kartaDataFromServerList.value[index].description,
                                    fontSize = 14.sp,
                                    color = Grey2
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun KartaImage(uri: String) {
    val painter = rememberAsyncImagePainter(model = uri)
    Box {
        Box(
            modifier = Modifier
                .border(
                    width = 4.dp,
                    color = ButtonBorder,
                    shape = RoundedCornerShape(5.dp)
                )
                .background(color = ButtonContainer.copy(alpha = 0.4f))
        ) {
            Image(
                modifier = Modifier
                    .height(98.dp)
                    .width(70.dp),
                painter = painter,
                contentDescription = null
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp, end = 3.dp)
                .size(30.dp)
                .background(Color.White, shape = CircleShape)
                .border(width = 3.dp, color = Grey, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "あ",
                fontSize = 14.sp,
                color = Grey,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.kiwimaru_regular))
            )
        }
    }
}