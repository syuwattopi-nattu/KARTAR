package com.example.myapplication.view.widget.textField

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.kartar.R
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.theme.LiteGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(authViewModel: AuthViewModel) {
    OutlinedTextField(
        value = authViewModel.password.value,
        onValueChange = { newValue ->
            authViewModel.onPasswordChanged(newValue)
        },
        singleLine = true,
        placeholder = {
            Text(
                text = "英数字で6～12文字で入力してください",
                fontSize = 12.sp,
                color = Grey2
            )
        },
        isError = authViewModel.isPasswordValid.value,
        label = {
            Text(
                text = "パスワード",
                color = DarkGreen,
                fontFamily = FontFamily(Font(R.font.kiwimaru_medium)),
                fontWeight = FontWeight.Bold,
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Grey,
            focusedBorderColor = DarkGreen,
            unfocusedBorderColor = LiteGreen,
            containerColor = ButtonContainer.copy(alpha = 0.5f)
        ),
        trailingIcon = {
            val image = if (authViewModel.passwordVisibility.value) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(
                onClick = { authViewModel.passwordVisibility.value = !authViewModel.passwordVisibility.value },
            ) {
                Icon(imageVector = image, contentDescription = "Toggle password visibility")
            }
        },
        visualTransformation = if (authViewModel.passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
    )
}