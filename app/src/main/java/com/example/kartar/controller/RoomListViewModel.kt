package com.example.kartar.controller

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.model.RoomData
import com.example.kartar.model.realTimeDatabase.RoomInfo
import com.example.kartar.view.AugmentedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class RoomListViewModel(context: Context) : ViewModel() {
    //検索ボックス用の変数
    val searchKeyword = mutableStateOf("")
    //かるたを保存しているディレクトリー一覧
    val kartaDirectories = mutableStateOf<List<File>>(listOf())
    //遊ぶかるたの選択
    val playKartaUid = mutableStateOf("")
    val playKartaTitle = mutableStateOf("")
    //ルームリスト
    val roomList = mutableStateOf<List<RoomData>>(listOf())

    init {
        getAllRoom()
    }

    //検索入力処理
    fun onSearchBoxChange(newString: String, context: Context, roomCreateViewModel: RoomCreateViewModel, navController: NavController) {
        if (newString.length < 17) {
            searchKeyword.value = newString
            if (searchKeyword.value.length == 16) {
                FirebaseSingleton.databaseReference.getReference("room/${searchKeyword.value}").get()
                    .addOnSuccessListener { dataSnap ->
                        val dialogBuilder = AlertDialog.Builder(context)
                        dialogBuilder.setTitle("部屋検索の結果")
                        if (dataSnap.value != null) {
                            dialogBuilder.setMessage("部屋がみつかりました!\n入りますか？")
                            dialogBuilder.setPositiveButton("OK") { _, _ ->
                                try {
                                    val createRoom = FirebaseSingleton.databaseReference.getReference("room").child(searchKeyword.value)
                                    //人数の追加
                                    createRoom.child("roomInfo").child("count").get()
                                        .addOnSuccessListener { snapshot ->
                                            val currentValue: Int? = snapshot.getValue(Int::class.java)
                                            if (currentValue != null && currentValue < 4) {
                                                createRoom.child("roomInfo").child("count").setValue(currentValue + 1)
                                                //ポイントの初期化
                                                val updateMap = HashMap<String, Any>()
                                                updateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = 0
                                                createRoom.child("point").updateChildren(updateMap)
                                                //参加者登録
                                                val playerUpdateMap = HashMap<String, Any>()
                                                playerUpdateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = "enter"
                                                createRoom.child("player").updateChildren(playerUpdateMap)
                                                roomCreateViewModel.enterRoomUid.value = searchKeyword.value
                                                roomCreateViewModel.roomInformation(navController, context = context)
                                            } else {
                                                Toast.makeText(context, "部屋に入ることに失敗しました", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } catch (e:Exception) {
                                    Log.d("エラー", e.message.toString())
                                }
                            }
                            dialogBuilder.setNegativeButton("NO") { dialog, _ ->
                                dialog.dismiss()  // ダイアログを閉じる
                            }
                            val dialog = dialogBuilder.create()
                            dialog.show()
                        } else {
                            dialogBuilder.setMessage("部屋が存在しません!")
                            dialogBuilder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            val dialog = dialogBuilder.create()
                            dialog.show()
                        }
                    }
            }
        }
    }

    //ディレクトリのかるた取得
    fun getKartaInDirectories(context: Context) {
        val dir = File(context.filesDir, "karta")
        if (dir.listFiles() != null) {
            kartaDirectories.value = dir.listFiles().filter { it.isDirectory }
        }
    }

    //ルーム一覧取得
    private fun getAllRoom() {
        val database = FirebaseDatabase.getInstance().reference
        val roomRef = database.child("room")

        roomRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentList = mutableListOf<RoomData>()
                for (roomSnapshot in snapshot.children) {
                    val roomInfo = roomSnapshot.child("roomInfo").getValue(RoomInfo::class.java)
                    if (roomInfo?.kind == "public" && roomInfo.start == "false") {
                        currentList.add(
                            RoomData(
                                name = roomInfo.roomName,
                                count = roomInfo.count,
                                isStart = roomInfo.start,
                                kind = roomInfo.kind,
                                roomUid = roomSnapshot.key.toString()
                            )
                        )
                    }
                }
                roomList.value = currentList
                Log.d("roomList", roomList.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
                Log.d("roomList", "e:${error.message}")
            }
        })
    }

    fun enterRoom(index: Int, navController: NavController, roomCreateViewModel: RoomCreateViewModel, context: Context) {
        viewModelScope.launch {
            try {
                val database = FirebaseDatabase.getInstance()
                val createRoom = database.getReference("room").child(roomList.value[index].roomUid)
                //人数の追加
                createRoom.child("roomInfo").child("count").get()
                    .addOnSuccessListener { snapshot ->
                        val currentValue: Int? = snapshot.getValue(Int::class.java)
                        if (currentValue != null && currentValue < 4) {
                            createRoom.child("roomInfo").child("count").setValue(currentValue + 1)
                            //ポイントの初期化
                            val updateMap = HashMap<String, Any>()
                            updateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = 0
                            createRoom.child("point").updateChildren(updateMap)
                            //参加者登録
                            val playerUpdateMap = HashMap<String, Any>()
                            playerUpdateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = "enter"
                            createRoom.child("player").updateChildren(playerUpdateMap)
                            roomCreateViewModel.enterRoomUid.value = roomList.value[index].roomUid
                            roomCreateViewModel.roomInformation(navController, context = context)
                        } else {
                            Toast.makeText(context, "参加できません..", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e:Exception) {
                Log.d("エラー", e.message.toString())
            }
        }
    }

    /**ランダム入出**/
    fun randomEnterRoom(navController: NavController, roomCreateViewModel: RoomCreateViewModel, context: Context) {
        try {
            if (roomList.value.isNotEmpty()) {
                val randomItem = roomList.value.random()
                val database = FirebaseDatabase.getInstance()
                val createRoom = database.getReference("room").child(randomItem.roomUid)
                //人数の追加
                createRoom.child("roomInfo").child("count").get()
                    .addOnSuccessListener { snapshot ->
                        val currentValue: Int? = snapshot.getValue(Int::class.java)
                        if (currentValue != null && currentValue < 4) {
                            createRoom.child("roomInfo").child("count").setValue(currentValue + 1)
                            //ポイントの初期化
                            val updateMap = HashMap<String, Any>()
                            updateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = 0
                            createRoom.child("point").updateChildren(updateMap)
                            //参加者登録
                            val playerUpdateMap = HashMap<String, Any>()
                            playerUpdateMap[FirebaseAuth.getInstance().currentUser?.uid.toString()] = "enter"
                            createRoom.child("player").updateChildren(playerUpdateMap)
                            roomCreateViewModel.enterRoomUid.value = randomItem.roomUid
                            roomCreateViewModel.roomInformation(navController, context = context)
                        } else {
                            Toast.makeText(context, "参加できません..", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e:Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    //ソロプレイ開始
    fun onClickSoloPlayStartButton(context: Context) {
        if (playKartaUid.value != "") {
            //画像を取得
            val dir = File(context.filesDir, "karta/${playKartaUid.value}")
            val allFiles = dir.listFiles()
            val imageFiles = allFiles?.filter {
                it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg"))
            }
            //渡すFilePath
            val filePathList = mutableListOf<Pair<String, String>>()
            //databaseに追加
            for (i in 0 until 44) {
                val imageName = imageFiles?.get(i)?.nameWithoutExtension.toString()
                val imageFile = imageFiles?.get(i)?.absoluteFile
                if (imageFile != null) {
                    filePathList.add(Pair(imageName, imageFile.path))
                }
            }
            val keys = filePathList.map { it.first }.toTypedArray()
            val values = filePathList.map { it.second }.toTypedArray()
            //画面遷移
            val intent = Intent(context, AugmentedActivity::class.java)
            intent.putExtra("KEYS", keys)
            intent.putExtra("VALUES", values)
            (context as Activity).startActivity(intent)
        } else {
            Toast.makeText(context, "かるたを選択してください", Toast.LENGTH_SHORT).show()
        }
    }
}