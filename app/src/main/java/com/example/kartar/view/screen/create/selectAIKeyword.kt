package com.example.kartar.view.screen.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.controller.CreateViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.Grey2
import com.example.kartar.view.widget.button.ButtonContent
import com.example.myapplication.view.widget.AppBar
import com.example.myapplication.view.widget.SimpleEditField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAIKeyword(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    createViewModel: CreateViewModel,
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AI作成キーワード",
                        color = Grey2,
                        fontSize = 20.sp
                    )
                    SimpleEditField(
                        value = createViewModel.AIKeyword.value,
                        onClick = { newValue -> createViewModel.onAIKeywordChange(newValue = newValue) },
                        isError = false,
                        label = "キーワード"
                    )
                    Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.4f))
                    ButtonContent(
                        modifier = ConstantsSingleton.widthButtonModifier,
                        onClick = { /*TODO*/ },
                        text = "作成する",
                        fontSize = ConstantsSingleton.widthButtonText,
                        fontWeight = FontWeight.Normal,
                        border = 4
                    )
                }
            }
        }
    }
    //サーバ処理中に表示するインディケーター
}

@Preview
@Composable
private fun SelectAIKeywordPreview() {
    SelectAIKeyword(navController = rememberNavController(), profileViewModel = viewModel(), createViewModel = viewModel())
}