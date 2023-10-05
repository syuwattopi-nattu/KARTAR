package com.example.kartar.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kartar.MainActivity
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.myapplication.view.widget.CircleUserIcon
import com.example.kartar.view.widget.button.ButtonContent

@Composable
fun HomeScreen(navController: NavController, profileViewModel: ProfileViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserIconComponent(navController = navController, profileViewModel = profileViewModel)
            SelectButtonsComponent(navController = navController)
        }
    }
}

@Composable
private fun UserIconComponent(navController: NavController, profileViewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.2f),
        contentAlignment = Alignment.Center
    ) {
        CircleUserIcon(
            modifier = Modifier.size(80.dp),
            profileViewModel = profileViewModel,
            borderWidth = 3,
            navController = navController
        )
    }
}

@Composable
private fun SelectButtonsComponent(navController: NavController) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(maxHeight * 0.1f)
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.05f))
            PlayKartaButton(navController = navController)
            EfudaButton(navController)
        }
    }
}

@Composable
fun PlayKartaButton(navController: NavController) {
    ButtonContent(
        onClick = { navController.navigate("roomList") },
        text = "かるたで\nあそぶ",
        modifier = ConstantsSingleton.standardButtonModifier,
        fontSize = ConstantsSingleton.standardButtonText
    )
}

@Composable
fun EfudaButton(navController: NavController) {
    ButtonContent(
        onClick = { navController.navigate(MainActivity.Screen.EfudaCollection.route) },
        text = "絵札",
        modifier = ConstantsSingleton.standardButtonModifier,
        fontSize = ConstantsSingleton.standardButtonText
    )
}