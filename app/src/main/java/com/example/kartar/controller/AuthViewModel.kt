package com.example.kartar.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.kartar.MainActivity
import com.example.kartar.R
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch

class AuthViewModel(private val profileViewModel: ProfileViewModel): ViewModel() {
    /*ログイン用の変数*/
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    /*パスワードを可視できるか*/
    val passwordVisibility = mutableStateOf(false)
    val isEmailValid = mutableStateOf(false)
    val isPasswordValid = mutableStateOf(false)
    val allowShowDialog = mutableStateOf(false)
    val showProcessIndicator = mutableStateOf(false)

    /**メールアドレスの入力処理**/
    fun onEmailChange(newValue: String) {
        val isValid = newValue.all {
            (it in 'a'..'z') || (it in 'A'..'Z') || (it in '0'..'9') ||
                    it == '@' || it == '.' || it == '-' || it == '_'
        }
        if (isValid) email.value = newValue
    }

    /**ユーザのパスワード入力処理**/
    fun onPasswordChanged(newValue: String) {
        val isAlphaNumeric = newValue.all { it.isLetterOrDigit() }
        val isBackspacePressed = newValue.length < password.value.length
        if (isAlphaNumeric && newValue.length <= 12 || isBackspacePressed)  password.value = newValue
    }

    /**メール・パスワードの入力チェック**/
    fun onValidCheck(): Boolean {
        isEmailValid.value = !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
        isPasswordValid.value = password.value.length < 6 || password.value.length > 12
        allowShowDialog.value = !isEmailValid.value && !isPasswordValid.value

        return !isEmailValid.value && !isPasswordValid.value
    }

    /**入力した情報を新規登録する処理**/
    fun signInUser(context: Activity, navController: NavController) {
        try {
            viewModelScope.launch {
                FirebaseSingleton.firebaseAuthReference.createUserWithEmailAndPassword(email.value, password.value)
                    .addOnSuccessListener {
                        allowShowDialog.value = false
                        Toast.makeText(context, "登録が完了しました!", Toast.LENGTH_SHORT).show()
                        /*ユーザ情報入力画面に移動*/
                        navController.navigate(MainActivity.Screen.ProfileSetup.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    .addOnFailureListener { exception ->
                        allowShowDialog.value = false
                        if (exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(context, "既に登録されています...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "登録が失敗しました...", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            allowShowDialog.value = false
            Toast.makeText(context, "登録が失敗しました...", Toast.LENGTH_SHORT).show()
        }
    }

    /**ユーザのログイン処理**/
    fun loginUser(navController: NavController, context: Activity) {
        /*入力に問題がないかチェック*/
        val onValid = onValidCheck()
        if (onValid) {
            try {
                viewModelScope.launch {
                    showProcessIndicator.value = true
                    /*ログイン処理*/
                    FirebaseSingleton.firebaseAuthReference.signInWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener { task ->
                            val uid = task.user?.uid
                            if (uid != null) {
                                getLoginUserData(context = context, navController = navController, uid = uid)
                            } else throw Exception()
                        }
                        .addOnFailureListener { exception ->
                            showProcessIndicator.value = false
                            Log.d("エラー", exception.message.toString())
                            Toast.makeText(context, "ログインに失敗しました", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: Exception) {
                showProcessIndicator.value = false
                Log.d("エラー", e.message.toString())
                Toast.makeText(context, "ログインに失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**ログイン情報からローカルにユーザ情報を保存**/
    private fun getLoginUserData(context: Context, navController: NavController, uid: String) {
        try {
            FirebaseSingleton.firestoreReference
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        /*ユーザ情報の取得*/
                        val userName = document.getString("userName")
                        val userIconImage = document.getString("iconImage")
                        /*ローカルに情報記録*/
                        val userSharedPreferences = profileViewModel.userSharedPreferences
                        val editor = userSharedPreferences.edit()
                        editor.putString(context.getString(R.string.UserName), userName.toString())
                        editor.putString(context.getString(R.string.imageIcon), userIconImage.toString())
                        if (editor.commit()) {
                            profileViewModel.userName.value = userName.toString()
                            profileViewModel.iconImageUri.value = userIconImage?.toUri()
                            Toast.makeText(context, "ログインが完了しました!", Toast.LENGTH_SHORT).show()
                            showProcessIndicator.value = false
                            /*HomeActivityに移動*/
                            navController.navigate(MainActivity.Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            context.startActivity(intent)
                            (context as Activity).finish()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showProcessIndicator.value = false
                    Log.d("エラー", exception.message.toString())
                    Toast.makeText(context, "ログインに失敗しました", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            showProcessIndicator.value = false
            Log.d("エラー", e.message.toString())
            Toast.makeText(context, "ログインに失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**ユーザのサインアウト処理**/
    fun signOutUser(navController: NavController, profileViewModel: ProfileViewModel) {
        profileViewModel.showSignOutDialog.value = false
        /*ユーザのサインアウト*/
        FirebaseSingleton.userSignOut()
        /*ローカルに記録していたユーザの情報削除*/
        with(profileViewModel.userSharedPreferences.edit()) {
            clear()
            apply()
            profileViewModel.userName.value == ""
        }
        /*未ログイン画面へ遷移*/
        navController.navigate("notLoggedIn") {
            popUpTo(navController.graph.startDestDisplayName) { inclusive = true }
        }
    }
}