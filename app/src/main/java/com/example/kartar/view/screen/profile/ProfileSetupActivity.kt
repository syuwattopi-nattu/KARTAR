package com.example.kartar.view.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.singleton.ConstantsSingleton
import com.example.kartar.theme.DarkGreen
import com.example.kartar.theme.LiteGreen
import com.example.kartar.view.widget.button.ButtonContent
import com.example.myapplication.view.widget.textField.UserNameTextField

@Composable
fun ProfileSetupScreen(profileViewModel: ProfileViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DescriptionTextComponent()
                UserSettingComponent(profileViewModel = profileViewModel)
                BottomButtonComponent(profileViewModel = profileViewModel)
            }
            /*サーバ処理中に表示するインディケーター*/
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

@Composable
private fun DescriptionTextComponent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.1f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "登録ありがとうございます!")
            Text(text = "次に自身のプロフィールを入力してください")
        }
    }
}

@Composable
private fun UserSettingComponent(profileViewModel: ProfileViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.8f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserIconImage(profileViewModel = profileViewModel)
            if (profileViewModel.isIconImageValid.value) {
                Text(
                    text = "アイコンを選択してください",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            UserNameTextField(profileViewModel = profileViewModel)
            if (profileViewModel.isUserNameValid.value) {
                Text(
                    text = "ユーザ名を入力してください",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
private fun BottomButtonComponent(profileViewModel: ProfileViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ProfileSaveButton(profileViewModel = profileViewModel)
    }
}

@Composable
fun UserIconImage(profileViewModel: ProfileViewModel) {
    /*フォルダから写真を選択した時の処理*/
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profileViewModel.localImageUri.value = uri
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (profileViewModel.localImageUri.value != null) {
            Image(
                painter = rememberAsyncImagePainter(profileViewModel.localImageUri.value),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = profileViewModel.circleColor.value,
                        shape = CircleShape
                    )
                    .size(170.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = profileViewModel.circleColor.value,
                        shape = CircleShape
                    )
                    .size(170.dp)
                    .clip(CircleShape)
            )
        }
        TextButton(
            onClick = { launcher.launch("image/*") }
        ) {
            Text(
                text = "アイコンをえらぶ",
                color = DarkGreen,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun ProfileSaveButton(profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    ButtonContent(
        modifier = ConstantsSingleton.widthButtonModifier,
        onClick = { profileViewModel.registerUserInformation(context) },
        text = "OK",
        border = 4,
        fontSize = ConstantsSingleton.widthButtonText,
        fontWeight = FontWeight.Normal
    )
}

@Preview
@Composable
fun ProfileSetupScreenPreview() {
    val context = LocalContext.current
    ProfileSetupScreen(ProfileViewModel(context, rememberNavController()))
}