package com.example.kartar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kartar.controller.AuthViewModel
import com.example.kartar.controller.CreateViewModel
import com.example.kartar.controller.KartaSearchViewModel
import com.example.kartar.controller.ProfileViewModel
import com.example.kartar.controller.RoomCreateViewModel
import com.example.kartar.controller.RoomListViewModel
import com.example.kartar.controller.viewModelFactory.AuthViewModelFactory
import com.example.kartar.controller.viewModelFactory.CreateViewModelFactory
import com.example.kartar.controller.viewModelFactory.ProfileViewModelFactory
import com.example.kartar.controller.viewModelFactory.RoomCreateViewModelFactory
import com.example.kartar.controller.viewModelFactory.RoomListViewModelFactory
import com.example.kartar.view.screen.HomeScreen
import com.example.kartar.view.screen.auth.LoginScreen
import com.example.kartar.view.screen.auth.NotLoggedInScreen
import com.example.kartar.view.screen.auth.SignInScreen
import com.example.kartar.view.screen.create.CreateMethodSelectScreen
import com.example.kartar.view.screen.create.EfudaCollectionScreen
import com.example.kartar.view.screen.create.KartaDetailScreen
import com.example.kartar.view.screen.create.OriginalCreateScreen
import com.example.kartar.view.screen.create.SelectAIKeyword
import com.example.kartar.view.screen.create.server.ServerEfudaCollectionScreen
import com.example.kartar.view.screen.create.server.ServerKartaDetail
import com.example.kartar.view.screen.playKarta.RoomCreateScreen
import com.example.kartar.view.screen.playKarta.RoomListScreen
import com.example.kartar.view.screen.playKarta.StandByRoomScreen
import com.example.kartar.view.screen.playKarta.solo.SoloSetupScreen
import com.example.kartar.view.screen.profile.ProfileSetupScreen
import com.example.kartar.view.screen.profile.UserProfileScreen
import com.example.myapplication.controller.MainViewModel

class MainActivity: ComponentActivity() {
    private var navigateToDestination: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigateToDestination = intent.getStringExtra("NAVIGATE_TO")

        setContent {
            MyAppScreen()
        }
    }

    @Composable
    fun MyAppScreen() {
        val navController = rememberNavController()
        val context = LocalContext.current
        /*viewModelの作成*/
        val mainViewModel: MainViewModel = viewModel()
        val kartaSearchViewModel: KartaSearchViewModel = viewModel()
        val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context, navController))
        val roomListViewModel: RoomListViewModel = viewModel(factory = RoomListViewModelFactory(context))
        val createViewModel: CreateViewModel = viewModel(factory = CreateViewModelFactory(context))
        val roomCreateViewModel: RoomCreateViewModel = viewModel(factory = RoomCreateViewModelFactory(context))
        val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(profileViewModel = profileViewModel))
        /*最初の画面遷移先を指定*/
        var startDestination: String = mainViewModel.getStartDestination(profileViewModel = profileViewModel)
        if (navigateToDestination != null) {
            startDestination = navigateToDestination as String
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            builder = {
                /*ホーム画面*/
                composable(Screen.Home.route) { HomeScreen(navController = navController, profileViewModel = profileViewModel) }
                /**Auth**/
                /*未ログイン時の画面*/
                composable(Screen.NotLoggedIn.route) { NotLoggedInScreen(navController = navController) }
                /*サインイン画面*/
                composable(Screen.Signin.route) { SignInScreen(navController, authViewModel) }
                /*ログイン画面*/
                composable(Screen.Login.route) { LoginScreen(navController, authViewModel = authViewModel) }
                /*******
                create
                 *********/
                /*かるた作成方法の選択画面*/
                composable(Screen.CreateMethodSelect.route) { CreateMethodSelectScreen(navController = navController ,profileViewModel = profileViewModel) }
                /*かるた一覧画面*/
                composable(Screen.EfudaCollection.route) {
                    EfudaCollectionScreen(
                        navController,
                        profileViewModel = profileViewModel,
                        createViewModel = createViewModel
                    )
                }
                /*かるたの詳細画面*/
                composable("${Screen.KartaDetail.route}/{kartaUid}") { navBackStackEntry ->
                    val kartaUid = navBackStackEntry.arguments?.getString("kartaUid").toString()
                    KartaDetailScreen(navController = navController, profileViewModel = profileViewModel, kartaUid = kartaUid, createViewModel = createViewModel)
                }
                /*AIかるた作成のキーワード入力画面*/
                composable(Screen.SelectAIKeyword.route) {
                    SelectAIKeyword(navController = navController, profileViewModel = profileViewModel, createViewModel = createViewModel)
                }
                /*自作かるたがめん*/
                composable(Screen.OriginalCreate.route) { OriginalCreateScreen(navController = navController, createViewModel = createViewModel, profileViewModel = profileViewModel) }
                /***************
                create>>server
                 ***************/
                /*サーバかるた一覧*/
                composable(Screen.ServerEfudaCollection.route) { ServerEfudaCollectionScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    kartaSearchViewModel = kartaSearchViewModel
                ) }
                /*さーばかるたの詳細*/
                composable("${Screen.ServerKartaDetail.route}/{kartaUid}") {navBackStackEntry ->
                    val kartaUid = navBackStackEntry.arguments?.getString("kartaUid").toString()
                    ServerKartaDetail(
                        createViewModel = createViewModel,
                        navController = navController,
                        profileViewModel = profileViewModel,
                        kartaSearchViewModel = kartaSearchViewModel,
                        kartaUid = kartaUid
                    ) }
                /**profile**/
                /*プロフィールの初期設定*/
                composable(Screen.ProfileSetup.route) { ProfileSetupScreen(profileViewModel = profileViewModel) }
                /*プロフィール確認画面*/
                composable(Screen.UserProfile.route) { UserProfileScreen(navController = navController, profileViewModel = profileViewModel, authViewModel = authViewModel) }
                /**playKarta**/
                /*部屋一覧表示*/
                composable(Screen.RoomList.route) { RoomListScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    roomListViewModel = roomListViewModel,
                    roomCreateViewModel
                ) }
                /*ルーム作成*/
                composable(Screen.RoomCreate.route) { RoomCreateScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    roomCreateViewModel = roomCreateViewModel
                ) }
                /*ルーム待機*/
                composable(Screen.StandByRoom.route) { StandByRoomScreen(
                    navController = navController,
                    roomCreateViewModel = roomCreateViewModel
                ) }
                /**playKarta>>solo**/
                //ソロプレイ用のScreen
                composable(Screen.SoloSetup.route) { SoloSetupScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    roomListViewModel = roomListViewModel
                ) }
            }
        )
    }

    sealed class Screen(val route: String) {
        object Home: Screen("home")
        object NotLoggedIn: Screen("notLoggedIn")
        object Signin: Screen("signin")
        object Login: Screen("login")
        object ProfileSetup: Screen("profileSetup")
        object EfudaCollection: Screen("efudaCollection")
        object KartaDetail: Screen("kartaDetail")
        object UserProfile: Screen("userProfile")
        object CreateMethodSelect: Screen("createMethodSelect")
        object OriginalCreate: Screen("originalCreate")
        object ServerEfudaCollection: Screen("serverEfudaCollection")
        object ServerKartaDetail: Screen("serverKartaDetail")
        object RoomList: Screen("roomList")
        object SoloSetup: Screen("soloSetup")
        object RoomCreate: Screen("roomCreate")
        object StandByRoom: Screen("standByRoom")
        object SelectAIKeyword: Screen("selectAIKeyword")
    }
}