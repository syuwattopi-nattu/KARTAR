package com.example.myapplication.view.widget

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.theme.LiteGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleEditField(
    value: String,
    onClick: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean,
    label: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onClick(newValue)
        },
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 12.sp,
                color = Grey2
            )
        },
        isError = isError,
        label = {
            Text(
                text = label,
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Grey,
            focusedBorderColor = DarkGreen,
            unfocusedBorderColor = LiteGreen,
            containerColor = ButtonContainer.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
    )
}