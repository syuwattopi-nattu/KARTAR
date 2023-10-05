package com.example.kartar.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.kartar.model.KARTAData
import com.example.kartar.model.KartaDataFromServer
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KartaSearchViewModel : ViewModel(){
    //検索ボックス用の変数
    val searchKeyword = mutableStateOf("")
    val searchBoxPlaceholder = mutableStateOf("1～21文字で入力してください")
    val searchBoxLabel = mutableStateOf("かるたの検索")
    private val searchType = mutableStateOf("normal")
    //かるたデータ取得用の変数
    val kartaDataFromServerList = mutableStateOf(
        listOf(KartaDataFromServer(kartaUid = "", create = "", title = "", description = "", genre = "", kartaImage = ""))
    )
    //かるた詳細表示に必要な変数
    val kartaTitle = mutableStateOf("")
    val kartaGenre = mutableStateOf("")
    val kartaDescription = mutableStateOf("")
    //取得したかるたの絵札・読み札を格納
    val kartaDataList = mutableStateOf(listOf(KARTAData("", "")))
    //かるたのダウンロードダイアログの表示
    val showDownloadDialog = mutableStateOf(false)
    val showIndicator = mutableStateOf(false)

    init {
        getAllKartaDataFromServer()
    }

    //検索方法の変化に対する処理
    fun changeSearchType() {
        //検索方法の変更
        when (searchType.value) {
            "normal" -> {
                searchType.value = "title"
                searchBoxPlaceholder.value = "1～15文字で入力してください"
                searchBoxLabel.value = "かるたのタイトルで検索"
            }
            "title" -> {
                searchType.value = "description"
                searchBoxPlaceholder.value = "1～21文字で入力してください"
                searchBoxLabel.value = "かるたの説明で検索"
            }
            "description" -> {
                searchType.value = "genre"
                searchBoxPlaceholder.value = "1～21文字で入力してください"
                searchBoxLabel.value = "かるたのジャンルで検索"
            }
            else -> {
                searchType.value = "normal"
                searchBoxPlaceholder.value = "1～21文字で入力してください"
                searchBoxLabel.value = "かるたの検索"
            }
        }
    }

    //検索ボックスの入力処理
    fun onClickServerKartaSearchBox(newValue: String) {
        when (searchType.value) {
            "title" -> {
                if (newValue.length < 16) {
                    searchKeyword.value = newValue
                }
            }
            else -> {
                if (newValue.length < 21) {
                    searchKeyword.value = newValue
                }
            }
        }
        getKartaDataFromServer()
    }

    //サーバからかるた情報を取得
    private fun getKartaDataFromServer() {
        if (FirebaseAuth.getInstance().currentUser?.uid != null) {
            viewModelScope.launch {
                val newKartaDataList = mutableListOf<KartaDataFromServer>()
                val firestore = FirebaseFirestore.getInstance()

                when (searchType.value) {
                    "normal" -> {
                        val querySnapshot = firestore.collection("kartaes").get().await()

                        for (document in querySnapshot.documents) {
                            val title = document.getString("title") ?: ""
                            val description = document.getString("description") ?: ""
                            val genre = document.getString("genre") ?: ""

                            if (title.contains(searchKeyword.value) || description.contains(searchKeyword.value) || genre.contains(searchKeyword.value)) {
                                val efuda = firestore.collection("kartaes")
                                    .document(document.id)
                                    .collection("efuda")
                                    .document("0")
                                    .get()
                                    .await()

                                newKartaDataList.add(
                                    KartaDataFromServer(
                                    kartaUid = document.id,
                                    create = document.get("create").toString(),
                                    title = title,
                                    description = description,
                                    genre = genre,
                                    kartaImage = efuda.get("efuda").toString()
                                )
                                )
                            }
                        }

                        kartaDataFromServerList.value = newKartaDataList
                    }
                    "title" -> {  //タイトル検索時
                        val querySnapshot = firestore.collection("kartaes").get().await()

                        for (document in querySnapshot.documents) {
                            val title = document.getString("title") ?: ""

                            if (title.contains(searchKeyword.value)) {
                                val efuda = firestore.collection("kartaes")
                                    .document(document.id)
                                    .collection("efuda")
                                    .document("0")
                                    .get()
                                    .await()

                                newKartaDataList.add(
                                    KartaDataFromServer(
                                    kartaUid = document.id,
                                    create = document.get("create").toString(),
                                    title = title,
                                    description = document.get("description").toString(),
                                    genre = document.get("genre").toString(),
                                    kartaImage = efuda.get("efuda").toString()
                                )
                                )
                            }
                        }

                        kartaDataFromServerList.value = newKartaDataList
                    }
                    "description" -> {
                        val querySnapshot = firestore.collection("kartaes").get().await()

                        for (document in querySnapshot.documents) {
                            val description = document.getString("description") ?: ""

                            if (description.contains(searchKeyword.value)) {
                                val efuda = firestore.collection("kartaes")
                                    .document(document.id)
                                    .collection("efuda")
                                    .document("0")
                                    .get()
                                    .await()

                                newKartaDataList.add(
                                    KartaDataFromServer(
                                    kartaUid = document.id,
                                    create = document.get("create").toString(),
                                    title = document.get("title").toString(),
                                    description = description,
                                    genre = document.get("genre").toString(),
                                    kartaImage = efuda.get("efuda").toString()
                                )
                                )
                            }
                        }

                        kartaDataFromServerList.value = newKartaDataList
                    }
                    else -> {
                        val querySnapshot = firestore.collection("kartaes").get().await()

                        for (document in querySnapshot.documents) {
                            val genre = document.getString("genre") ?: ""

                            if (genre.contains(searchKeyword.value)) {
                                val efuda = firestore.collection("kartaes")
                                    .document(document.id)
                                    .collection("efuda")
                                    .document("0")
                                    .get()
                                    .await()

                                newKartaDataList.add(
                                    KartaDataFromServer(
                                    kartaUid = document.id,
                                    create = document.get("create").toString(),
                                    title = document.get("title").toString(),
                                    description = document.get("description").toString(),
                                    genre = genre,
                                    kartaImage = efuda.get("efuda").toString()
                                )
                                )
                            }
                        }

                        kartaDataFromServerList.value = newKartaDataList
                    }
                }
            }
        }
    }

    private fun getAllKartaDataFromServer() {
        if (FirebaseAuth.getInstance().currentUser?.uid != null) {
            val newKartaDataList = mutableListOf<KartaDataFromServer>()
            val firestore = FirebaseFirestore.getInstance()

            viewModelScope.launch {
                val querySnapshot = firestore.collection("kartaes").get().await()

                for (document in querySnapshot.documents) {
                    val efuda = firestore.collection("kartaes")
                        .document(document.id)
                        .collection("efuda")
                        .document("0")
                        .get()
                        .await()

                    newKartaDataList.add(
                        KartaDataFromServer(
                        kartaUid = document.id,
                        create = document.get("create").toString(),
                        title = document.get("title").toString(),
                        description = document.get("description").toString(),
                        genre = document.get("genre").toString(),
                        kartaImage = efuda.get("efuda").toString()
                    )
                    )
                }

                kartaDataFromServerList.value = newKartaDataList
                Log.d("newValue", kartaDataFromServerList.value.toString())
            }
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        continuation.invokeOnCancellation {
            this.isCanceled
        }
    }

    //選択したかるたの情報取得
    fun getKartaInformation(kartaUid: String) {
        viewModelScope.launch {
            if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                FirebaseFirestore.getInstance().collection("kartaes").document(kartaUid).get()
                    .addOnSuccessListener { data ->
                        kartaTitle.value = data.get("title").toString()
                        kartaGenre.value = data.get("genre").toString()
                        kartaDescription.value = data.get("description").toString()
                    }
            }
        }
    }

    val operationComplete: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    //選択したかるたの読み札・絵札取得
    fun getYomifudaAndEfuda(kartaUid: String) {
        if (FirebaseAuth.getInstance().currentUser?.uid != null){
            val firestore = FirebaseFirestore.getInstance()
            val currentList = mutableListOf<KARTAData>()
            viewModelScope.launch {

                for (index in 0 until 44) {
                    val efudaTask = firestore.collection("kartaes").document(kartaUid)
                        .collection("efuda").document(index.toString()).get().await()

                    val yomifudaTask = firestore.collection("kartaes").document(kartaUid)
                        .collection("yomifuda").document(index.toString()).get().await()

                    currentList.add(
                        KARTAData(
                        efuda = efudaTask.get("efuda").toString(),
                        yomifuda = yomifudaTask.get("yomifuda").toString()
                    )
                    )
                }
                operationComplete.value = true
                kartaDataList.value = currentList
            }
        }
    }


    //かるたをダウンロードする処理
    @OptIn(DelicateCoroutinesApi::class)
    fun downloadKarta(context: Context, kartaUid: String, navController: NavController) {
        viewModelScope.launch {
            showDownloadDialog.value = false
            if (kartaDataList.value.size == 44) {
                showIndicator.value = true
                Log.d("listtt", kartaDataList.value.toString())
                try {
                    val sharedPreferences = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val dir = File(context.filesDir, "karta/$kartaUid")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    editor.putString("state", "他人")
                    editor.putString("title", kartaTitle.value)
                    editor.putString("description", kartaDescription.value)
                    editor.putString("genre", kartaGenre.value)
                    editor.putString("uid", kartaUid)
                    val jobs = mutableListOf<Job>()
                    kartaDataList.value.forEachIndexed { index, item ->
                        editor.putString(index.toString(), item.yomifuda)
                        val job = launch(Dispatchers.IO) {
                            val bitmap = Glide.with(context).asBitmap().load(item.efuda).submit().get()
                            val file = File(dir, "$index.png")
                            FileOutputStream(file).use { stream ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                Log.d("stream", "画像を保存した$index")
                            }
                        }
                        jobs.add(job)
                    }
                    jobs.forEach { it.join() }
                    editor.apply()
                    withContext(Dispatchers.Main) {
                        showDownloadDialog.value = false
                        showIndicator.value = false
                        navController.popBackStack()
                        Toast.makeText(context, "保存しました", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("エラー", e.message.toString())
                        Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show()
                        showIndicator.value = false
                    }
                }
            } else {
                Toast.makeText(context, "少々おまちください...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}