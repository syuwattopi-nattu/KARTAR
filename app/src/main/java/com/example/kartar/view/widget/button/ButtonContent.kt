package com.example.kartar.view.widget.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kartar.R
import com.example.kartar.theme.ButtonBorder
import com.example.kartar.theme.ButtonContainer
import com.example.kartar.theme.Grey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    border: Int = 8,
    fontSize: Int = 30,
    fontWeight: FontWeight = FontWeight.Bold,
    borderColor: Color = ButtonBorder
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(3.dp),
        onClick = { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = border.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(3.dp)
                )
                .background(ButtonContainer, shape = RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = Grey,
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                fontFamily = FontFamily(Font(R.font.kiwimaru_medium))
            )
        }
    }
}