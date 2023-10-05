package com.example.kartar.view.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.kartar.R
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.Grey
import com.example.kartar.theme.Grey2
import com.example.kartar.theme.LiteGreen
import com.example.kartar.view.widget.button.ButtonContent
import com.example.myapplication.view.widget.textField.UserNameTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController, profileViewModel: ProfileViewModel, authViewModel: AuthViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            topBar = { PopBackAppBar(navController = navController) }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    UserInformationComponent(profileViewModel = profileViewModel)
                    BottomButtonComponent(profileViewModel = profileViewModel)
                    /*名前変更するダイアログ*/
                    if (profileViewModel.showUserNameDialog.value) {
                        UserNameChangeDialog(profileViewModel = profileViewModel)
                    /*サインアウト用のダイアログ*/
                    } else if (profileViewModel.showSignOutDialog.value) {
                        SignOutDialog(
                            profileViewModel = profileViewModel,
                            authViewModel = authViewModel,
                            navController = navController
                        )
                    }
                }
                /*サーバ処理中に表示*/
                if (profileViewModel.showProcessIndicator.value) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopBackAppBar(navController: NavController) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() },
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = null,
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            navigationIconContentColor = Grey2,
            titleContentColor = Grey
        )
    )
}

@Composable
private fun UserInformationComponent(profileViewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.8f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserIcon(profileViewModel)
            Spacer(modifier = Modifier.fillMaxHeight(fraction = 0.1f))
            UserNameText(profileViewModel)
        }
    }
}

@Composable
private fun BottomButtonComponent(profileViewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        SignOutButton(profileViewModel = profileViewModel)
    }
}

@Composable
fun UserIcon(profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                profileViewModel.changeUserImageIcon(uri, context)
            }
        }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = profileViewModel.iconImageUri.value,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = LiteGreen,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .size(170.dp)
                .align(Alignment.Center)
        )
        IconButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 60.dp)
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = Icons.Default.Cached,
                contentDescription = "Toggle password visibility",
                // 色はお好みで設定してください
                tint = DarkGreen,
            )
        }
    }
}

@Composable
private fun UserNameText(profileViewModel: ProfileViewModel) {
    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = profileViewModel.userName.value,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center),
                color = Grey,
                fontFamily = FontFamily(Font(R.font.kiwimaru_medium))
            )
            Divider(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp),
                color = Grey2
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { profileViewModel.showUserNameDialog.value = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = DarkGreen,
                )
            }
        }
    }
}

@Composable
fun SignOutButton(profileViewModel: ProfileViewModel) {
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = { profileViewModel.showSignOutDialog.value = true },
        text = "ログアウトする",
        fontSize = ConstantsSingleton.widthButtonText,
        border = 4
    )
}

@Composable
fun UserNameChangeDialog(profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    Dialog(onDismissRequest = { profileViewModel.showUserNameDialog.value = true }) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "新しい名前を入力してください",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                UserNameTextField(profileViewModel = profileViewModel)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        profileViewModel.onClickUserNameDialog(context = context)
                    }
                ) {
                    Text(text = "OK", color = LiteGreen, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SignOutDialog(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
    ) {
    AlertDialog(
        onDismissRequest = { profileViewModel.showSignOutDialog.value = false },
        title = { Text(text = "最終確認", color = DarkGreen)},
        text = {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "ログアウトしてもよろしいですか？", color = Grey)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                profileViewModel.showSignOutDialog.value = false
            }) {
                Text(text = "NO", color = Grey2, fontSize = 16.sp)
            }
            TextButton(
                onClick = { authViewModel.signOutUser(navController = navController, profileViewModel = profileViewModel)}
            ) {
                Text(text = "OK", color = DarkGreen, fontSize = 16.sp)
            }
        }
    )
}

@Preview
@Composable
private fun UserProfileScreenPreview() {
    UserProfileScreen(rememberNavController(), profileViewModel = viewModel(), authViewModel = viewModel())
}