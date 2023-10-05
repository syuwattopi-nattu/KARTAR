package com.example.myapplication.view.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.theme.Grey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleUserIcon(
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    borderWidth: Int,
    navController: NavController,
) {
    Surface(
        shape = CircleShape,
        onClick = {
            navController.navigate("userProfile")
        }
    ) {
        AsyncImage(
            model = profileViewModel.iconImageUri.value.toString(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = modifier
                .border(
                    width = borderWidth.dp,
                    color = Grey,
                    shape = CircleShape
                )
                .clip(CircleShape)
        )
    }
}