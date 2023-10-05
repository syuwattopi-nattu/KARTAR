package com.example.kartar.view.screen.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.LiteGreen
import com.example.kartar.view.widget.button.ButtonContent
import com.example.kartar.view.widget.textField.EmailTextField
import com.example.myapplication.view.widget.textField.PasswordTextField

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DescriptionTextComponent()
                InputFieldComponent(authViewModel = authViewModel)
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    OnValidButton(navController = navController, authViewModel = authViewModel)
                }
            }
            /*サーバ処理中に表示するインディケーター*/
            if (authViewModel.showProcessIndicator.value) {
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
    }
}

@Composable
private fun DescriptionTextComponent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.2f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = "登録した\nメールアドレス&パスワードを入力してください"
        )
    }
}

@Composable
private fun InputFieldComponent(authViewModel: AuthViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.7f),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.2f))
            /*Email入力*/
            EmailTextField(authViewModel)
            if (authViewModel.isEmailValid.value) {
                Text(
                    text = "有効なメールアドレスを入力してください",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.05f))
            /*パスワード入力*/
            PasswordTextField(authViewModel)
            if (authViewModel.isPasswordValid.value) {
                Text(
                    text = "有効なパスワードを入力してください",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun OnValidButton(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = {
            val onValid = authViewModel.onValidCheck()
            if (onValid) {
                authViewModel.loginUser(
                    navController = navController,
                    context = context as Activity
                )
            }
                  },
        text = "OK",
        border = 4,
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal
    )
}

@Preview
@Composable
fun LoginScreenView() {
    LoginScreen(rememberNavController(), AuthViewModel(profileViewModel = ProfileViewModel(context = LocalContext.current, navController = rememberNavController())))
}
