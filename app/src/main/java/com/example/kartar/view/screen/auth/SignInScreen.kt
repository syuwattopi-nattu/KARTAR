package com.example.kartar.view.screen.auth

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.view.widget.button.ButtonContent
import com.example.kartar.view.widget.textField.EmailTextField
import com.example.myapplication.view.widget.textField.PasswordTextField

@Composable
fun SignInScreen(navController: NavController, authViewModel: AuthViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DescriptionTextComponent()
            InputFieldComponent(authViewModel = authViewModel)
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                OnValidButton(authViewModel = authViewModel)
            }
        }
    }
    if (authViewModel.allowShowDialog.value) {
        CheckAlertDialog(authViewModel = authViewModel, navController)
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
            text = "メールアドレス&パスワードを入力してください",
            color = Grey,
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
private fun OnValidButton(authViewModel: AuthViewModel) {
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = { authViewModel.onValidCheck() },
        text = "OK",
        border = 4,
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun CheckAlertDialog(authViewModel: AuthViewModel, navController: NavController) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { authViewModel.allowShowDialog.value = false },
        title = { Text(text = "最終確認", color = DarkGreen)},
        text = {
               Column {
                   Spacer(modifier = Modifier.height(20.dp))
                   Text(text = "入力した情報で登録します", color = Grey)
                   Spacer(modifier = Modifier.height(10.dp))
                   Text(text = "よろしいでしょうか？", color = Grey)
               }
        },
        confirmButton = {
            TextButton(onClick = {
                authViewModel.allowShowDialog.value = false
            }) {
                Text(text = "NO", color = Grey2, fontSize = 16.sp)
            }
            TextButton(
                onClick = { authViewModel.signInUser((context as Activity), navController = navController)}
            ) {
                Text(text = "OK", color = DarkGreen, fontSize = 16.sp)
            }
        }
    )
}

@Preview
@Composable
fun SignInScreenView() {
    SignInScreen(rememberNavController(), AuthViewModel(profileViewModel = viewModel()))
}