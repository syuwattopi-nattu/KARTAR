package com.example.kartar.controller

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.kartar.MainActivity
//import com.example.myapplication.MainActivity
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.model.realTimeDatabase.RoomInfo
import com.example.kartar.model.realTimeDatabase.gameInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AugmentedController : ViewModel(){
    /*部屋の情報*/
    val roomUid = mutableStateOf("")
    val roomState = mutableStateOf("")
    val ownerUid = mutableStateOf("")
    /*ゲームの情報*/
    private val playedCount = mutableIntStateOf(0)
    private val nextGet = mutableStateOf("あ")
    private var nextStartTime: Long = Calendar.getInstance().timeInMillis
    /*時間になったことを伝えるHandler*/
    private val handler = Handler(Looper.getMainLooper())

    /**初期化**/
    private fun initProcess() {
        roomUid.value = ""
        roomState.value = ""
        ownerUid.value = ""
        playedCount.intValue = 0
        nextGet.value = "あ"
    }

    /**ホストの人が部屋の状態を管理する**/

    private var playerStateListener: ValueEventListener? = null
    fun upDatePlayerState(context: Context) {
        val ref = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}")
        try {
            playerStateListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /*全員状態がrestの場合*/
                    val allRest = snapshot.child("player").children.all {
                        it.getValue(String::class.java) == "rest"
                    }
                    if (allRest) {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start")
                            .setValue("standBy")
                    }
                    /*全員状態がplayingの場合*/
                    val allPlaying = snapshot.child("player").children.all {
                        it.getValue(String::class.java) == "playing"
                    }
                    if (allPlaying) {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("game")
                    }
                    /*全員状態がendの場合*/
                    val allEnd = snapshot.child("player").children.all {
                        it.getValue(String::class.java) == "end"
                    }
                    val gameInfo = snapshot.child("gameInfo").getValue(gameInfo::class.java)
                    /*ゲーム終了処理*/
                    if ((gameInfo?.play!! <= gameInfo.now) && allEnd) {

                        Log.d("終了", "aaa")
                        ref.removeEventListener(playerStateListener!!)
                        roomUid.value = ""
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                        ref.setValue(null)
                        initProcess()
                    } else if (allEnd) {
                        try {
                            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}").get()
                                .addOnSuccessListener { getSnapshot ->
                                    val playerCount = getSnapshot.child("player").childrenCount
                                    val playedCount = getSnapshot.child("time").childrenCount
                                    /*全員プレイ済みの場合*/
                                    if (playerCount == playedCount) {
                                        pointProcess(getSnapshot = getSnapshot)
                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("end")
                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time").setValue(null)
                                    }
                                    Log.d("人数", playerCount.toString())
                                }
                        } catch (e: Exception) {
                            Log.d("エラー", e.message.toString())
                        }
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("result")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
            ref.addValueEventListener(playerStateListener!!)
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**点数配分処理**/
    private fun pointProcess(getSnapshot: DataSnapshot) {
        try {
            val timeData = mutableMapOf<String, String>()
            getSnapshot.child("time").children.forEach {
                it.key.let { key ->
                    timeData[key.toString()] = it.value.toString()
                }
            }
            val sortedEntries = timeData.entries.sortedBy { it.value }
            /*点数振り分け*/
            val playRef = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/point")
            sortedEntries.forEachIndexed { index, entry ->
                if (getSnapshot.child("time").childrenCount.toInt() != 4) {
                    Log.d("点数", entry.key)
                    when (index) {
                        0 -> {
                            Log.d("点数", entry.toString())
                            val currentPoint = getSnapshot.child("point/${entry.key}").getValue(Int::class.java)
                            if (currentPoint != null) {
                                playRef.child(entry.key).setValue(currentPoint + 1)
                            }
                        }
                    }
                } else {
                    when (index) {
                        0 -> {
                            val currentPoint = getSnapshot.child("point/${entry.key}").getValue(Int::class.java)
                            if (currentPoint != null) {
                                playRef.child(entry.key).setValue(currentPoint + 2)
                            }
                        }
                        1 -> {
                            val currentPoint = getSnapshot.child("point/${entry.key}").getValue(Int::class.java)
                            if (currentPoint != null) {
                                playRef.child(entry.key).setValue(currentPoint + 1)
                            }
                        }
                    }
                }
            }
            val nowData = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/gameInfo/now")
            nowData.get()
                .addOnSuccessListener { nowSnapShot ->
                    val currentNow = nowSnapShot.getValue(Int::class.java)
                    if (currentNow != null) {
                        nowData.setValue(currentNow + 1)
                    }
                }
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**部屋の状態に変化があったときの各処理**/
    fun upDateRoomState(context: Context) {
        try {
            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomInfo = snapshot.getValue(RoomInfo::class.java)
                    roomInfo?.apply {
                        roomState.value = roomInfo.start
                        when (roomState.value) {
                            "standBy" -> {
                                standByStateProcess()
                            }
                            "game" -> {
                                gameStateProcess(context = context)
                            }
                            else -> {
                                FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                                    .setValue("rest")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    throw Exception(error.message)
                }
            })
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**部屋の状態が「standBy」だった場合の処理**/
    fun standByStateProcess() {
        try {
            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/gameInfo").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val gameInfo = snapshot.getValue(gameInfo::class.java)
                    gameInfo?.apply {
                        playedCount.intValue = gameInfo.now
                        nextGet.value = snapshot.child("hiragana/${playedCount.intValue}").getValue(String::class.java).toString()
                        /*自身の状態を更新*/
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                            .setValue("playing")
                            .addOnFailureListener { e ->

                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    throw Exception(error.message)
                }
            })
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**部屋の状態が「standBy」だった場合の処理**/
    fun gameStateProcess(context: Context) {
        try {
            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/gameInfo").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val gameInfo = snapshot.getValue(gameInfo::class.java)
                    gameInfo?.apply {
                        addTimeToCurrent(gameInfo.next)
                        scheduleToast(context)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    throw Exception(error.message)
                }
            })
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**文字列から時間を取得して次かるたを再生する時間を指定**/
    private fun addTimeToCurrent(time: String) {
        val parts = time.split(":")
        val minutesToAdd = parts[0].toInt()
        val secondsToAdd = parts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutesToAdd)
        calendar.add(Calendar.SECOND, secondsToAdd)

        nextStartTime = calendar.timeInMillis - Calendar.getInstance().timeInMillis
    }

    /**指定時間に処理を行う処理**/
    private fun scheduleToast(context: Context) {
        Log.d("定刻", "is$nextStartTime")
        handler.postDelayed({
            try {
                /*TODO:取得した時間を記録する*/
                FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time/${FirebaseSingleton.currentUid()}").setValue("01:30")
                    .addOnSuccessListener {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                            .setValue("end")
                    }
            } catch (e: Exception) {
                Log.d("エラー", e.message.toString())
            }
            Log.d("定刻", "Setting delay to: $nextStartTime milliseconds")
            Toast.makeText(context, nextGet.value, Toast.LENGTH_SHORT).show()
        }, nextStartTime)
    }
}