package com.example.kartar.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.kartar.R
import com.example.kartar.view.AugmentedActivity
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.model.EnterPlayer
import com.example.kartar.model.KARTAData
import com.example.kartar.model.realTimeDatabase.RoomInfo
import com.example.kartar.model.realTimeDatabase.gameInfo
import com.example.kartar.model.realTimeDatabase.owner
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.random.Random

class RoomCreateViewModel(context: Context) : ViewModel() {
    private val sharedPref: SharedPreferences = context.getSharedPreferences(context.getString(R.string.UserInformation), Context.MODE_PRIVATE)
    val roomIsPublic = mutableStateOf(true)
    //部屋の名前
    val roomName = mutableStateOf("")
    //val roomPassword = mutableStateOf("public")
    private val roomUid = mutableStateOf("")
    val playKartaUid = mutableStateOf("")
    val isRoomNameValid = mutableStateOf(false)
    //val isRoomPasswordValid = mutableStateOf(false)
    val selectedOption = mutableIntStateOf(1)
    //保存されたかるた一覧
    val kartaDirectories = mutableStateOf<List<File>>(listOf())
    val playKartaTitle = mutableStateOf("")
    //入った部屋
    val roomState = mutableStateOf("")
    val enterRoomName = mutableStateOf("")
    val enterRoomUid = mutableStateOf("")
    val standByPlayer = mutableStateOf(0)
    val ownerUid = mutableStateOf("")
    val allPlayers = mutableStateOf<List<EnterPlayer>>(listOf())
    val playerInformation = mutableStateOf<List<Pair<String, String>>>(listOf())

    init {
        roomUid.value = UUID.randomUUID().toString().replace("-", "")
        getKartaDir(context = context)
    }

    //かるたディレクトリから名前を取得
    private fun getKartaDir(context: Context) {
        val dir = File(context.filesDir, "karta")
        kartaDirectories.value = dir.listFiles()
            ?.filter { it.isDirectory }
            ?.filter {
                val sharedPreferences = context.getSharedPreferences(it.nameWithoutExtension, Context.MODE_PRIVATE)
                val valuePref = sharedPreferences.getString("state", "サーバ")
                valuePref != "ローカル"
            }
            ?: listOf()
    }

    /**制作した部屋の名前入力処理**/
    fun onRoomNameChange(newString: String) {
        if (newString.length < 16) roomName.value = newString
    }

    /**制作した部屋のパスワード入力処理**/
    fun onRoomPasswordChange(newString: String) {
        val regex = Regex("^[a-zA-Z0-9]*$")
        if (newString.length < 16 && regex.matches(newString)) {
            //roomPassword.value = newString
        }
    }

    /**ゲーム部屋の作成処理**/
    fun gameRoomCreate(context: Context, navController: NavController) {
        val net = isNetworkAvailable(context)  /*ネットに接続しているかの確認*/
        if (roomName.value != "" && playKartaUid.value != "" && net) {
            try {
                viewModelScope.launch {
                    val createRoomDatabase = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}")
                    /*ルームにオーナーの情報を記入*/
                    createRoomDatabase.child("owner").setValue(
                        owner(
                            uid = FirebaseSingleton.currentUid() ?: ""
                        )
                    ).addOnFailureListener { exception ->
                        throw Exception(exception)
                    }
                    /*自身のポイントを初期化*/
                    createRoomDatabase.child("point").setValue(
                        mapOf(
                            FirebaseSingleton.currentUid().toString() to 0
                        )
                    ).addOnFailureListener { exception ->
                        throw Exception(exception)
                    }
                    /*ゲーム部屋の情報を初期化*/
                    val kind = if (roomIsPublic.value) {
                        "public"
                    } else "private"
                    createRoomDatabase.child("roomInfo").setValue(
                        RoomInfo(
                            kind = kind,
                            roomName = roomName.value
                        )
                    ).addOnFailureListener { exception ->
                        throw Exception(exception)
                    }
                    /*この部屋のゲーム詳細の設定*/
                    createRoomDatabase.child("gameInfo").setValue(
                        gameInfo(
                            kartaUid = playKartaUid.value,
                            next = randomTimeBetween("00:10", "00:30"),
                            play = selectedOption.intValue
                        )
                    ).addOnFailureListener { exception ->
                        throw Exception(exception)
                    }
                    /*今回のゲームで使うかるたを保存*/
                    val hiraganaList = listOf("あ", "い", "う", "え", "お", "か", "き", "く", "け", "こ", "さ", "し", "す", "せ", "そ", "た", "ち", "つ", "て", "と", "な", "に", "ぬ", "ね", "の", "は", "ひ", "ふ", "へ", "ほ", "ま", "み", "む", "め", "も", "や", "ゆ", "よ", "ら", "り", "る", "れ", "ろ", "わ")
                    val selectedHiragana = hiraganaList.shuffled().take(selectedOption.intValue)
                    val map = selectedHiragana.mapIndexed { index, hiragana -> index to hiragana }.toMap()
                    val dataToSave = mutableMapOf<String, String>()
                    map.forEach { (key, value) ->
                        dataToSave[key.toString()] = value
                    }
                    createRoomDatabase.child("gameInfo/hiragana").setValue(dataToSave)
                        .addOnFailureListener { exception ->
                            throw Exception(exception)
                        }
                    /*部屋の値をあらためて取得*/
                    enterRoomUid.value = roomUid.value
                    roomState.value = "false"
                    roomInformation(navController, context)
                    Toast.makeText(context, "部屋の作成に成功しました", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                /*部屋が中途半端に存在しないように削除*/
                val createRoomDatabase = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}")
                createRoomDatabase.setValue(null)
                Log.d("エラー", e.message.toString())
                Toast.makeText(context, "部屋の作成に失敗しました", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    /**部屋の退出or解散処理**/
    fun exitRoom(navController: NavController) {
        navController.popBackStack("roomList", false)
        viewModelScope.launch {
            try {
                val createRoom = FirebaseSingleton.databaseReference.getReference("room").child(enterRoomUid.value)
                if (ownerUid.value == FirebaseAuth.getInstance().currentUser?.uid) {
                    /*オーナの場合部屋を解散する*/
                    createRoom.setValue(null)
                } else {
                    /*参加者の場合部屋を退出する*/
                    createRoom.child("roomInfo").child("count").get()
                        .addOnSuccessListener { snapshot ->
                            val currentValue: Int? = snapshot.getValue(Int::class.java)
                            if (currentValue != null) {
                                createRoom.child("roomInfo").child("count").setValue(currentValue - 1)
                                //ポイント削除
                                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                                createRoom.child("point").child(userId).setValue(null)
                                //プレーヤー削除
                                createRoom.child("player").child(userId).setValue(null)
                            }
                        }
                }
            } catch (e:Exception) {
                Log.d("エラー", e.message.toString())
            }
        }
    }

    /**ゲーム開始処理**/
    fun gameStart(context: Context) {
        try {
            viewModelScope.launch {
                val room = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player")
                room.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val allOK = snapshot.children.all { 
                            it.getValue(String::class.java) == "ok"
                        }
                        if (allOK) {
                            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("true")
                            sendAugmentImageActivity(context)
                            Toast.makeText(context, "画面を移動します", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "まだ準備中の人がいます!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(context, "データベースエラー: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**AugmentedImageActivityに画面遷移**/
    fun sendAugmentImageActivity(context: Context) {
        try {
            /*かるたの画像を取得*/
            val dir = File(context.filesDir, "karta/${playKartaUid.value}")
            val allFiles = dir.listFiles()
            val imageFiles = allFiles?.filter {
                it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg"))
            }
            /*渡すFilePath*/
            val filePathList = mutableListOf<Pair<String, String>>()
            /*databaseに追加*/
            for (i in 0 until 44) {
                val imageName = imageFiles?.get(i)?.nameWithoutExtension.toString()
                val imageFile = imageFiles?.get(i)?.absoluteFile
                if (imageFile != null) {
                    filePathList.add(Pair(imageName, imageFile.path))
                }
            }
            val keys = filePathList.map { it.first }.toTypedArray()
            val values = filePathList.map { it.second }.toTypedArray()
            stopListeningToRoomInformation()
            /*AugmentImageに画面遷移*/
            val intent = Intent(context, AugmentedActivity::class.java)
            intent.putExtra("KEYS", keys)
            intent.putExtra("VALUES", values)
            intent.putExtra("ROOMUID", roomUid.value)
            intent.putExtra("OWNERUID", ownerUid.value)
            (context as Activity).startActivity(intent)
            //Toast.makeText(context, "全員OK", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
            Log.d("エラー", playKartaUid.value)
            Toast.makeText(context, "画面遷移に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**ゲーム部屋の状態確認**/
    private var roomInformationListener: ValueEventListener? = null
    fun roomInformation(navController: NavController, context: Context) {
        val enterRoom = FirebaseSingleton.databaseReference.getReference("room").child(enterRoomUid.value)
        navController.navigate("standByRoom")
        roomInformationListener = enterRoom.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    navController.navigate("roomList")
                }else {
                    //現在の参加人数取得
                    val roomInfo = snapshot.child("roomInfo").getValue(RoomInfo::class.java)
                    Log.d("へや", roomInfo.toString())
                    if (roomInfo != null) {
                        roomState.value = roomInfo.start
                        standByPlayer.value = roomInfo.count
                        Log.d("へや", standByPlayer.value.toString())
                    }
                    enterRoomName.value = roomInfo?.roomName ?: "対戦部屋ルーム"
                    //現在の参加者取得
                    val currentPlayer = mutableListOf<EnterPlayer>()
                    snapshot.child("player").children.forEach { playerSnapshot ->
                        currentPlayer.add(
                            EnterPlayer(
                            uid = playerSnapshot.key.toString(),
                            state = playerSnapshot.value.toString()
                        )
                        )
                    }
                    //オーナ情報取得
                    val owner = snapshot.child("owner").getValue(owner::class.java)
                    ownerUid.value = owner?.uid ?: ""
                    //かるた情報
                    val gameInfo = snapshot.child("gameInfo").child("kartaUid").getValue(String::class.java)
                    playKartaUid.value = gameInfo.toString()
                    Log.d("stream", "分岐:${gameInfo}")
                    if (!File(context.filesDir, "karta/${gameInfo}").exists()) {
                        if (gameInfo != null) {
                            Log.d("stream", "かるたdownload")
                            val firestore = FirebaseFirestore.getInstance()
                            val currentList = mutableListOf<KARTAData>()
                            val kartaUid = gameInfo
                            var kartaTitle = ""
                            var kartaDescription = ""
                            var kartaGenre = ""
                            viewModelScope.launch {
                                firestore.collection("kartaes").document(kartaUid).get()
                                    .addOnSuccessListener { snapshot ->
                                        kartaTitle = snapshot.get("title").toString()
                                        kartaDescription = snapshot.get("description").toString()
                                        kartaGenre = snapshot.get("genre").toString()
                                    }
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

                                if (currentList.size == 44) {
                                    Log.d("listtt", currentList.toString())
                                    try {
                                        val sharedPreferences = context.getSharedPreferences(kartaUid, Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        val dir = File(context.filesDir, "karta/$kartaUid")
                                        if (!dir.exists()) {
                                            dir.mkdirs()
                                        }
                                        editor.putString("state", "他人")
                                        editor.putString("title", kartaTitle)
                                        editor.putString("description", kartaDescription)
                                        editor.putString("genre", kartaGenre)
                                        editor.putString("uid", kartaUid)
                                        val jobs = mutableListOf<Job>()
                                        currentList.forEachIndexed { index, item ->
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
                                            Toast.makeText(context, "保存しました", Toast.LENGTH_SHORT).show()
                                            val database = FirebaseDatabase.getInstance()
                                            val userRef = database.getReference("room")
                                                .child(enterRoomUid.value)
                                                .child("player")
                                                .child(FirebaseAuth.getInstance().uid.toString())
                                            userRef.get()
                                                .addOnSuccessListener { stateSnapshot ->
                                                    if (stateSnapshot.getValue(String::class.java) == "false") {
                                                        userRef.setValue("ok")
                                                    }
                                                }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Log.d("エラー", e.message.toString())
                                            Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val userRef = FirebaseSingleton.databaseReference.getReference("room")
                            .child(enterRoomUid.value)
                            .child("player")
                            .child(FirebaseAuth.getInstance().uid.toString())

                        userRef.get()
                            .addOnSuccessListener { stateSnapshot ->
                                if (stateSnapshot.getValue(String::class.java) == "false") {
                                    userRef.setValue("ok")
                                }
                            }
                        Log.d("定刻", "あああ")
                    }
                    allPlayers.value = currentPlayer
                    getPlayerProfile()
                    if (roomState.value == "true") {
                        sendAugmentImageActivity(context = context)
                        roomState.value = "false"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    fun stopListeningToRoomInformation() {
        val enterRoom = FirebaseSingleton.databaseReference.getReference("room").child(enterRoomUid.value)
        if (roomInformationListener != null) {
            enterRoom.removeEventListener(roomInformationListener!!)
            roomInformationListener = null
        }
    }

    fun getPlayerProfile() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        if (currentUserUid != null) {
            val firestore = FirebaseFirestore.getInstance()
            for (player in allPlayers.value) {
                val task = firestore.collection("users").document(player.uid).get()
                tasks.add(task)
            }

            Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { snapshots ->
                val currentList = snapshots.map {
                    Pair(
                        it.get("userName").toString(),
                        it.get("iconImage").toString()
                    )
                }
                playerInformation.value = currentList
                Log.d("リスト", playerInformation.toString())
            }
        } else {
            playerInformation.value = emptyList()
        }
    }

    private fun randomTimeBetween(start: String, end: String): String {
        // 時間を秒単位に変換する関数
        fun timeToSeconds(time: String): Int {
            val (minutes, seconds) = time.split(":").map { it.toInt() }
            return minutes * 60 + seconds
        }

        val startSeconds = timeToSeconds(start)
        val endSeconds = timeToSeconds(end)

        // ランダムな秒数を取得
        val randomSeconds = Random.nextInt(startSeconds, endSeconds + 1)

        // 秒数を "分:秒" 形式に変換
        val minutes = randomSeconds / 60
        val seconds = randomSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }
}