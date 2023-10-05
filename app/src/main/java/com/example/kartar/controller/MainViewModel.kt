package com.example.kartar.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.kartar.controller.singleton.FirebaseSingleton

class MainViewModel: ViewModel(){
    /**ユーザの名前をローカルから取得**/
    private fun getUserName(profileViewModel: ProfileViewModel): String {
        return profileViewModel.userSharedPreferences.getString("UserName", "").toString()
    }

    /**起動時の画面遷移の設定**/
    fun getStartDestination(profileViewModel: ProfileViewModel): String {
        return try {
            FirebaseSingleton.ensureLoggedIn()
            if (FirebaseSingleton.currentUid() != null) {
                val userName = getUserName(profileViewModel = profileViewModel)
                if (userName == "") "profileSetUp" else "home"
            } else {
                "notLoggedIn"
            }
        } catch (e: Exception) {
            Log.d("エラー", "ログイン失敗:${e.message}")
            "notLoggedIn"
        }
    }
}