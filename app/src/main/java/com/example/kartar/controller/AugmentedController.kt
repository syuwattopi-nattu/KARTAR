package com.example.kartar.controller

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kartar.GameResultActivity
import com.example.kartar.MainActivity
//import com.example.myapplication.MainActivity
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.model.realTimeDatabase.RoomInfo
import com.example.kartar.model.realTimeDatabase.gameInfo
import com.google.ar.core.Session
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AugmentedController : ViewModel(), TextToSpeech.OnInitListener {
    /*部屋の情報*/
    val roomUid = mutableStateOf("")
    val roomState = mutableStateOf("")
    val ownerUid = mutableStateOf("")
    val yomifuda = mutableStateOf<List<String>>(listOf())
    /*ゲームの情報*/
    private val playedCount = mutableIntStateOf(0)
    val nextGet = mutableStateOf("あ")
    private var nextStartTime: Long = Calendar.getInstance().timeInMillis
    /*時間になったことを伝えるHandler*/
    private val handler = Handler(Looper.getMainLooper())
    /*NFC取得*/
    val nfcEnable = mutableStateOf(false)
    val playStartTime = mutableLongStateOf(Calendar.getInstance().timeInMillis)

    private var nfcText: String? = null

    private var textToSpeech: TextToSpeech? = null
    val speechAllow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**ホストの人が部屋の状態を管理する**/
    private var playerStateListener: ValueEventListener? = null
    fun upDatePlayerState() {
        val ref = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player")
        try {
            playerStateListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    /*全員状態がrestの場合*/
                    val allRest = snapshot.children.all {
                        it.getValue(String::class.java) == "rest"
                    }
                    if (allRest) {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time").setValue(null)
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/gameInfo").get()
                            .addOnSuccessListener { success ->
                                val gameInfo = success.getValue(gameInfo::class.java)
                                if (gameInfo != null) {
                                    if ((gameInfo.play <= gameInfo.now)) {
                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start")
                                            .setValue("gameSet")
                                    } else {
                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start")
                                            .setValue("standBy")
                                    }
                                }
                            }
                    }
                    /*全員状態がplayingの場合*/
                    val allPlaying = snapshot.children.all {
                        it.getValue(String::class.java) == "playing"
                    }
                    if (allPlaying) {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("game")
                    }
                    /*全員状態がendの場合*/
                    val allEnd = snapshot.children.all {
                        it.getValue(String::class.java) == "end"
                    }
                    if (allEnd) {
                        try {
                            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}").get()
                                .addOnSuccessListener { getSnapshot ->
                                    val playerCount = getSnapshot.child("player").childrenCount
                                    val playedCount = getSnapshot.child("time").childrenCount
                                    /*全員プレイ済みの場合*/
                                    if (playerCount == playedCount) {
                                        pointProcess(getSnapshot = getSnapshot)
                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo/start").setValue("end")
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

    /**部屋の状態に変化があったときの各処理**/
    private var roomStateListener: ValueEventListener? = null
    fun upDateRoomState(context: Context) {
        try {
            val roomRef = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/roomInfo")
            roomStateListener = object : ValueEventListener {
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
                            "end" -> {
                                try {
                                    FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}").get()
                                        .addOnSuccessListener { getSnapshot ->
                                            val timeData = mutableMapOf<String, String>()
                                            getSnapshot.child("time").children.forEach {
                                                it.key.let { key ->
                                                    timeData[key.toString()] = it.value.toString()
                                                }
                                            }
                                            val sortedEntries = timeData.entries.sortedBy { it.value }

                                            val index = sortedEntries.indexOfFirst { it.key == FirebaseSingleton.currentUid() }
                                            if (index != -1) {
                                                val alertDialog = AlertDialog.Builder(context)
                                                    .setTitle("結果がでました")
                                                    .setMessage("あなたは${index + 1}位です!\n取得時間:${sortedEntries[index].value}")
                                                    .setPositiveButton("OK") { _, _ ->
                                                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                                                            .setValue("rest")
                                                    }
                                                    .create()

                                                alertDialog.show()
                                            }
                                        }
                                } catch (e: Exception) {
                                    Log.d("エラー", e.message.toString())
                                }
                            }
                            "gameSet" -> {
                                roomRef.removeEventListener(roomStateListener!!)
                                val dialogBuilder = AlertDialog.Builder(context)
                                dialogBuilder.setTitle("ゲーム終了")
                                dialogBuilder.setMessage("ゲームが終了しました！\n結果発表にはいります")
                                dialogBuilder.setPositiveButton("OK") { _, _ ->
                                    Log.d("終了", "aaa")
                                    /*
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    intent.putExtra("NAVIGATE_TO", MainActivity.Screen.RoomList.route)
                                    context.startActivity(intent)
                                     */
                                    Log.d("uid", roomUid.value)
                                    val intent = Intent(context, GameResultActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    intent.putExtra("ROOM_UID", roomUid.value)
                                    context.startActivity(intent)
                                    val ref = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}")
                                    ref.removeEventListener(playerStateListener!!)
                                }
                                val dialog = dialogBuilder.create()
                                dialog.show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    throw Exception(error.message)
                }
            }
            roomRef.addValueEventListener(roomStateListener!!)
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

    /**部屋の状態が「standBy」だった場合の処理**/
    fun standByStateProcess() {
        try {
            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time").setValue(null)
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

    /**
     * NFCの正誤判定
     */
    fun setNfcText(text: String, context: Context) {
        Log.d("setNfcText", "正誤反転をします")
        // NFCから読み取ったテキストとnextGetの値を比較
        val isTextEqualNextGet = text.last().toString() == nextGet.value

        if (isTextEqualNextGet) {
            // ここにトゥルーの場合の処理を記述
            val endTime = System.currentTimeMillis()
            val elapsedTimeMillis = endTime - playStartTime.longValue
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) % 60

            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time/${FirebaseSingleton.currentUid()}").setValue("$minutes:$seconds")
                .addOnSuccessListener {
                    FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                        .setValue("end")
                }
            nfcEnable.value = false
            Log.d("setNfcText", "テキストが一致しました: $text")
        } else {
            // ここにファルスの場合の処理を記述
            otetukiDialog(context = context)
            Log.d("setNfcText", "テキストが一致しません: NFCから読み取ったテキスト: $text, nextGetの値: ${nextGet.value}")
        }
    }

    /**
     * おてつきdialogの表示
     */
    private fun otetukiDialog(context: Context) {
        nfcEnable.value = false
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("おてつきです!")
            .setMessage("減点します...")
            .create()

        alertDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (alertDialog.isShowing) {
                alertDialog.dismiss()
            }
            nfcEnable.value = true
        }, 3000) // 2秒後に閉じる
        try {
            FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/point/${FirebaseSingleton.currentUid()}").get()
                .addOnSuccessListener { currentPoint ->
                    val pointValue = currentPoint.getValue(Int::class.java)
                    if (pointValue != null && pointValue > 0) {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/point/${FirebaseSingleton.currentUid()}")
                            .setValue(pointValue - 1)
                    }
                }
        } catch (e: Exception) {
            Log.d("エラー", e.message.toString())
        }
    }

    /**指定時間に処理を行う処理**/
    private fun scheduleToast(context: Context) {
        Log.d("定刻", "is$nextStartTime")
        handler.postDelayed({
            speechAllow.value = true
            try {
                nfcEnable.value = true

                /*TODO:取得した時間を記録する*/
                /*
                FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/time/${FirebaseSingleton.currentUid()}").setValue("01:30")
                    .addOnSuccessListener {
                        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
                            .setValue("end")
                    }

                 */
            } catch (e: Exception) {
                Log.d("エラー", e.message.toString())
            }
            Log.d("定刻", "Setting delay to: $nextStartTime milliseconds")
            Toast.makeText(context, nextGet.value, Toast.LENGTH_SHORT).show()
        }, nextStartTime)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("speach", "成功")
            textToSpeech?.let { tts ->
                val locale = Locale.JAPAN
                if (tts.isLanguageAvailable(locale) > TextToSpeech.LANG_AVAILABLE) {
                    tts.language = Locale.JAPAN
                } else {
                    // 言語の設定に失敗
                }
            }

        } else {
            Log.d("speach", "失敗")
            // Tts init 失敗
        }
    }
}