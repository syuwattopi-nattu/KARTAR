package com.example.kartar.controller

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.kartar.R
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.theme.LiteGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context, private val navController: NavController) : ViewModel(){
    /*ユーザ情報を格納したSharedPreference*/
    val userSharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.UserInformation), Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = userSharedPreferences.edit()
    /*ユーザ情報の変数*/
    val userName = mutableStateOf(userSharedPreferences.getString(context.getString(R.string.UserName), "").toString())
    val iconImageUri = mutableStateOf(userSharedPreferences.getString(context.getString(R.string.imageIcon), "")?.toUri())
    /*フォルダから選択した画像のURI*/
    val localImageUri = mutableStateOf<Uri?>(null)
    /*validチェック用変数*/
    val isUserNameValid = mutableStateOf(false)
    val isIconImageValid = mutableStateOf(false)

    val circleColor = mutableStateOf(LiteGreen)
    val showProcessIndicator = mutableStateOf(false)
    val showUserNameDialog = mutableStateOf(false)
    val showSignOutDialog = mutableStateOf(false)

    /**ユーザネームの入力処理**/
    fun onUserNameChanged(newValue: String) {
        if (newValue.length < 17) userName.value = newValue
    }

    /**ユーザ名・アイコンをサーバに登録**/
    fun registerUserInformation(context: Context) {
        /*ユーザ名・アイコンのvalidチェック*/
        isUserNameValid.value = userName.value.isEmpty() || userName.value.length > 16
        isIconImageValid.value = localImageUri.value == null
        /*ユーザアイコンの色*/
        circleColor.value = if (isIconImageValid.value) Color.Red else LiteGreen
        /*サーバに設定した情報を記録*/
        if (!isUserNameValid.value && !isIconImageValid.value) {
            viewModelScope.launch {
                try {
                    showProcessIndicator.value = true
                    iconImageUploadToStorage(context = context)
                } catch (e: Exception) {
                    Log.d("エラー", e.message.toString())
                }
            }
        }
    }

    /**設定したアイコンをstorageに保存**/
    private fun iconImageUploadToStorage(context: Context) {
        val storageReference = FirebaseSingleton.storageReference.reference
        val uid = FirebaseSingleton.currentUid()
        try {
            /*画像の保存先*/
            val imageRef = storageReference.child("iconImages/${uid}")
            val uploadTask = localImageUri.value?.let { imageRef.putFile(it) }
            uploadTask?.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    /*firestoreの処理に移動*/
                    userUploadToFirestore(context = context, downloadUri = downloadUri)
                }.addOnFailureListener { exception ->
                    throw Exception(exception)
                }
            }?.addOnFailureListener { exception ->
                throw Exception(exception)
            }
        } catch (e: Exception) {
            showProcessIndicator.value = false
            Log.d("エラー", e.message.toString())
            Toast.makeText(context, "アイコンの保存に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**設定したユーザ情報をfirestoreに保存**/
    private fun userUploadToFirestore(context: Context, downloadUri: Uri) {
        try {
            /*現在ログインしている場合の処理*/
            FirebaseSingleton.ensureLoggedIn()
            /*firestoreに保存*/
            FirebaseSingleton.firestoreReference
                .collection("users")
                .document(FirebaseSingleton.currentUid() ?: "")
                .set(hashMapOf(
                    "userName" to userName.value,
                    "iconImage" to downloadUri
                )).addOnSuccessListener {
                    userUpdateToLocal(context = context, downloadUri = downloadUri)
                }
        } catch (e: Exception) {
            showProcessIndicator.value = false
            Log.d("エラー", e.message.toString())
            Toast.makeText(context, "サーバの保存に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**ローカルにユーザ情報を書き込む**/
    private fun userUpdateToLocal(context: Context, downloadUri: Uri) {
        try {
            editor.putString(context.getString(R.string.UserName), userName.value)
            editor.putString(context.getString(R.string.imageIcon), downloadUri.toString())
            editor.apply()
            if (editor.commit()) {
                /*変数の更新*/
                userName.value = userSharedPreferences.getString(context.getString(R.string.UserName), "").toString()
                iconImageUri.value = userSharedPreferences.getString(context.getString(R.string.imageIcon), "")?.toUri()
                /*HomeScreenに移動*/
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            /*変数の更新*/
            showProcessIndicator.value = false
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**アイコンの変更処理**/
    fun changeUserImageIcon(uri: Uri, context: Context) {
        iconImageUri.value = uri
        val uid = FirebaseSingleton.currentUid()
        if (FirebaseAuth.getInstance().currentUser?.uid != null) {
            val storageRef = FirebaseSingleton.storageReference.reference
            val imageRef = storageRef.child("iconImages/${uid}")
            viewModelScope.launch {
                showProcessIndicator.value = true
                try {
                    /*アイコン画像をstorageに保存*/
                    val uploadTask = iconImageUri.value?.let { imageRef.putFile(it) }
                    uploadTask?.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            //サーバに情報を書き込み
                            if (uid != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("iconImage", downloadUri)
                            }
                            //ローカルに情報を書き込み
                            editor.putString(context.getString(R.string.imageIcon), downloadUri.toString())
                            if (editor.commit()) {
                                iconImageUri.value = userSharedPreferences.getString(context.getString(
                                    R.string.imageIcon), "")?.toUri()
                                showProcessIndicator.value = false
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "更新に失敗しました...ネットワーク環境を確かめてください", Toast.LENGTH_SHORT).show()
                            showProcessIndicator.value = false
                        }
                    }?.addOnFailureListener{
                        Toast.makeText(context, "更新に失敗しました...", Toast.LENGTH_SHORT).show()
                        showProcessIndicator.value = false
                    }
                } catch (e: Exception) {
                    Log.d("エラー", "エラーでたよ～")
                    showProcessIndicator.value = false
                }
            }
        }
    }

    /**ユーザ名の変更ダイアログ入力処理**/
    fun onClickUserNameDialog(context: Context) {
        isUserNameValid.value = userName.value.length > 16
        if (userName.value == "") {
            showUserNameDialog.value = false
        } else if (isUserNameValid.value) {
            Toast.makeText(context, "入力にミスがあります", Toast.LENGTH_SHORT).show()
            showUserNameDialog.value = false
        } else {
            viewModelScope.launch {
                try {
                    /*ユーザの名前をfirestoreに情報を書き込み*/
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseSingleton.currentUid().toString())
                        .update("userName", userName.value)
                    /*ローカルに情報を書き込み*/
                    editor.putString(context.getString(R.string.UserName), userName.value)
                    if (editor.commit()) {
                        /*変数の更新*/
                        userName.value = userSharedPreferences.getString(context.getString(R.string.UserName), "").toString()
                        showUserNameDialog.value = false
                        Toast.makeText(context, "名前を変更しました", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("e", "エラーでたよ～")
                    showUserNameDialog.value = false
                }
            }
        }
    }
}