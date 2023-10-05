package com.example.myapplication.view.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kartar.controller.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavController, profileViewModel: ProfileViewModel) {
    TopAppBar(
        modifier = Modifier
            .height(70.dp)
            .padding(
                top = 14.dp,
                end = 20.dp
            ),
        title = { },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() },
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "Toggle password visibility",
                )
            }
        },
        actions = {
            CircleUserIcon(
                modifier = Modifier.size(40.dp),
                profileViewModel = profileViewModel,
                borderWidth = 1,
                navController = navController
            )
        }
    )
}