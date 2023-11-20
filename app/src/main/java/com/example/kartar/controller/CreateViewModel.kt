package com.example.kartar.controller

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.kartar.MainActivity
import com.example.kartar.model.KARTAData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CreateViewModel(context: Context) : ViewModel() {
    /*検索ボックス用の変数*/
    private val searchBoxText = mutableStateOf("")
    val isSearchBoxTextValid = mutableStateOf(false)
    /*かるたを保存しているディレクトリ一覧*/
    val kartaDirectories = mutableStateOf<List<File>>(listOf())
    //オリジナルかるた作成用の変数
    val kartaDataList = mutableStateOf(listOf(KARTAData("", "")))
    val hiraganaList = listOf("あ", "い", "う", "え", "お", "か", "き", "く", "け", "こ", "さ", "し", "す", "せ", "そ", "た", "ち", "つ", "て", "と", "な", "に", "ぬ", "ね", "の", "は", "ひ", "ふ", "へ", "ほ", "ま", "み", "む", "め", "も", "や", "ゆ", "よ", "ら", "り", "る", "れ", "ろ", "わ")
    val isYomifudaValid = mutableStateOf(false)
    //タイトルと説明文を入力するダイアログ表示
    val showInputTitleDialog = mutableStateOf(false)
    val kartaTitle = mutableStateOf("")
    val kartaDescription = mutableStateOf("")
    val kartaGenre = mutableStateOf("")
    val isKartaTitleValid = mutableStateOf(false)
    val isKartaDescriptionValid = mutableStateOf(false)
    val isKartaGenreValid = mutableStateOf(false)
    //かるた削除のダイアログ
    val showKartaDeleteDialog = mutableStateOf(false)
    //インディケーター
    val showProcessIndicator = mutableStateOf(false)

    init {
        kartaDataListInitialization()
        getKartaDirectories(context)
    }

    /**検索ボックスの入力処理**/
    fun onSearchBoxChange(newValue: String) {
        if (newValue.length  < 21) searchBoxText.value = newValue
    }

    /**かるた作成前の初期化処理**/
    private fun kartaDataListInitialization() {
        kartaDataList.value = hiraganaList.map { KARTAData(efuda = "", yomifuda = it) }
    }

    /**ローカルのかるたディレクトリー取得**/
    fun getKartaDirectories(context: Context) {
        val dir = File(context.filesDir, "karta")
        val validDirectories = mutableListOf<File>()

        dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
            val imageFiles = subDir.listFiles { _, name -> name.endsWith(".jpg") || name.endsWith(".png") }
            if (imageFiles?.size == 44) {
                validDirectories.add(subDir)
            } else {
                // 画像が44枚ではない場合、そのサブディレクトリのファイルをすべて削除
                deleteRecursively(subDir)
            }
        }
        kartaDirectories.value = validDirectories
    }

    /**かるた作成時の各読み札入力処理**/
    fun onChangeYomifuda(newValue: String, index: Int) {
        if (newValue.length <= 20) {
            val currentList = kartaDataList.value
            if (index in currentList.indices) {
                val updateItem = currentList[index].copy(yomifuda = newValue)
                val updateList = currentList.toMutableList().apply {
                    this[index] = updateItem
                }
                kartaDataList.value = updateList
            }
        }
    }

    fun onChangeEfuda(newValue: Uri, index: Int) {
        val currentList = kartaDataList.value.toMutableList()
        if (index in currentList.indices) {
            val updateItem = currentList[index].copy(efuda = newValue.toString())
            currentList[index] = updateItem
        }
        kartaDataList.value = currentList
    }

    //かるた保存ボタンを押した時の処理
    fun onClickSaveButton() {
        val allConditionMet = kartaDataList.value.withIndex().all { (index, kartaData) ->
            val firstCharOfYomifuda = kartaData.yomifuda.firstOrNull()
            firstCharOfYomifuda == hiraganaList[index].first() && kartaData.efuda != ""
        }
        if (allConditionMet) {
            showInputTitleDialog.value = true
            isYomifudaValid.value = false
        } else {
            isYomifudaValid.value = true
        }
    }

    /**作成したかるたのタイトル入力処理**/
    fun onChangeKartaTitle(newValue: String) {
        if (newValue.length < 16) kartaTitle.value = newValue
    }

    /**作成したかるたの説明文入力処理**/
    fun onChangeKartaDescription(newValue: String) {
        if (newValue.length < 21)  kartaDescription.value = newValue
    }

    /**作成したかるたのジャンル文入力処理**/
    fun onChangeKartaGenre(newValue: String) {
        if (newValue.length < 21)  kartaGenre.value = newValue
    }

    /**作成したかるたをローカルに保存**/
    fun saveKartaToLocal(context: Context, navController: NavController) {
        isKartaTitleValid.value = kartaTitle.value == ""
        isKartaDescriptionValid.value = kartaDescription.value == ""
        try {
            if (!isKartaTitleValid.value && !isKartaDescriptionValid.value) {
                /*作成したかるたのuid作成*/
                val kartaUid = UUID.randomUUID().toString().replace("-", "")
                val sharedPref = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                /*作成したかるたを保存するローカルディレクトリを作成*/
                val dir = File(context.filesDir, "karta/$kartaUid")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                /*設定したかるたをローカルに保存*/
                for (index in kartaDataList.value.indices) {
                    val imageFile = File(dir, "$index.png")
                    val inputStream = context.contentResolver.openInputStream(kartaDataList.value[index].efuda.toUri())
                    val outputStream = FileOutputStream(imageFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input?.copyTo(output)
                        }
                    }
                    editor.putString(index.toString(), kartaDataList.value[index].yomifuda)
                }
                //かるたのタイトル・説明文保存
                editor.putString("title", kartaTitle.value)
                editor.putString("description", kartaDescription.value)
                editor.putString("genre", kartaGenre.value)
                editor.putString("uid", kartaUid)
                editor.putString("state", "ローカル")
                if (editor.commit()) {
                    showInputTitleDialog.value = false
                    Toast.makeText(context, "保存に成功しました", Toast.LENGTH_SHORT).show()
                    kartaDataListInitialization()
                    getKartaDirectories(context = context)
                    navController.navigate(MainActivity.Screen.EfudaCollection.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    //ローカルのかるたをサーバに送信する処理
    fun uploadKarta(context: Context, kartaUid: String) {
        val dir = File(context.filesDir, "karta/$kartaUid")
        val sharedPreferences = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
        val filesToUpload = dir.listFiles()?.filter { it.extension in listOf("jpg", "jpeg", "png") }
        val totalFiles = filesToUpload?.size ?: 0
        var successCount = 0
        //さーばにかるた保存
        showProcessIndicator.value = true
        try {
            //最初にかるたのタイトル・作者などの情報を追加
            if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("kartaes")
                    .document(kartaUid)
                    .set(
                        hashMapOf(
                            "title" to sharedPreferences.getString("title", "かるたのタイトル"),
                            "description" to sharedPreferences.getString("description", "かるたの説明"),
                            "genre" to sharedPreferences.getString("genre", "かるたのジャンル"),
                            "create" to (FirebaseAuth.getInstance().currentUser?.uid ?: "KARTAR")
                        )
                    )
                filesToUpload?.forEach { file ->
                    if (file.extension in listOf("jpg", "jpeg", "png")) {
                        val fileUri = Uri.fromFile(file)
                        val storageRef = FirebaseStorage.getInstance().reference
                        val imageRef = storageRef.child("karta/${kartaUid}/${file.name}")
                        val uploadTask = imageRef.putFile(fileUri)
                        uploadTask.addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener {downloadUri ->
                                Log.d("fileName", "実行:${file.nameWithoutExtension}:count${successCount}")
                                //読み札の保存
                                FirebaseFirestore.getInstance()
                                    .collection("kartaes")
                                    .document(kartaUid)
                                    .collection("yomifuda")
                                    .document(file.nameWithoutExtension)
                                    .set(hashMapOf(
                                        "yomifuda" to sharedPreferences.getString(file.nameWithoutExtension, "よみふだ"))
                                    )
                                FirebaseFirestore.getInstance()
                                    .collection("kartaes")
                                    .document(kartaUid)
                                    .collection("efuda")
                                    .document(file.nameWithoutExtension)
                                    .set(
                                        hashMapOf("efuda" to downloadUri.toString())
                                    )
                                    .addOnSuccessListener {
                                        Log.d("fileName", "success:${file.name}:count${successCount}")
                                        successCount++
                                        if (successCount == totalFiles) {
                                            val editor = sharedPreferences.edit()
                                            editor.putString("state", "サーバ")
                                            editor.apply()
                                            Toast.makeText(context, "サーバに登録しました", Toast.LENGTH_SHORT).show()
                                            showProcessIndicator.value = false
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(context, "サーバに登録失敗しました...", Toast.LENGTH_SHORT).show()
                                        showProcessIndicator.value = false
                                        Log.d("fileName", "失敗:${file.name}:count${successCount}")
                                        return@addOnFailureListener
                                    }
                            }
                        }.addOnFailureListener{
                            Toast.makeText(context, "サーバに登録失敗しました...", Toast.LENGTH_SHORT).show()
                            showProcessIndicator.value = false
                            return@addOnFailureListener
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("ミス", e.message.toString())
            Toast.makeText(context, "ミス", Toast.LENGTH_SHORT).show()
        }
    }

    /**かるたを削除する処理**/
    fun kartaDelete(navController: NavController, kartaUid: String, context: Context) {
        /*かるたの絵札削除*/
        val contextFilesDir = context.filesDir
        val directoryToDelete = File(contextFilesDir, "karta/${kartaUid}")
        deleteRecursively(directoryToDelete)
        /*かるたの読み札削除*/
        val sharedPreferences = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        context.deleteSharedPreferences(kartaUid)
        /*画面遷移*/
        getKartaDirectories(context)
        navController.popBackStack()
        showKartaDeleteDialog.value = false
    }

    /**ディレクトリ内の画像をすべて削除**/
    private fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                deleteRecursively(it)
            }
        }
        return file.delete()
    }

    /*************AI作成部分**********************/

    val AIKeyword = mutableStateOf("")

    /**AIキーワード入力処理**/
    fun onAIKeywordChange(newValue: String) {
        if (newValue.length < 15)  AIKeyword.value = newValue
    }
}