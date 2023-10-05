package com.example.myapplication.view.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.kartar.R
import com.example.kartar.theme.Grey

@Composable
fun CircleYomifudaText(
    modifier: Modifier,
    text: String,
    size: Int = 60,
    fontSize: Int = 28
) {
    Box(
        modifier = modifier
            .padding(top = 3.dp, end = 3.dp)
            .size(size.dp)
            .background(Color.White, shape = CircleShape)
            .border(width = 3.dp, color = Grey, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = Grey,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.kiwimaru_regular))
        )
    }
}