package com.example.myapplication.view.widget.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.LiteGreen
import com.example.kartar.theme.Yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnValidCheckButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.shadow(10.dp),
        onClick = { onClick() }
    ) {
        Box(
            modifier
                .border(width = 4.dp, color = LiteGreen)
                .height(46.dp)
                .background(Yellow)
                .width(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                modifier = Modifier,
                color = DarkGreen,
                fontSize = 16.sp,
            )
        }
    }
}