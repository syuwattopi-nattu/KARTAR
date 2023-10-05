package com.example.kartar.view.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.R
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.DarkGreen
import com.example.kartar.view.widget.button.ButtonContent

@Composable
fun NotLoggedInScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(fraction = 0.6f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kartar_logo),
                    contentDescription = "KARTARのロゴ",
                    modifier = Modifier
                        .width(320.dp)
                        .height(200.dp)
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SignInButton(navController)
                    LogInTextButton(navController = navController)
                }
            }
        }
    }
}

@Composable
fun SignInButton(navController: NavController) {
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        border = 4,
        onClick = { navController.navigate("signin") },
        text = "サインイン",
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun LogInTextButton(navController: NavController) {
    TextButton(
        onClick = {
            navController.navigate("login")
        }
    ) {
        Text(
            text = "ログインはこちらから",
            modifier = Modifier,
            color = DarkGreen,
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.kiwimaru_medium))
        )
    }
}

@Preview
@Composable
fun AuthLoginScreenPreview() {
    NotLoggedInScreen(navController = rememberNavController())
}