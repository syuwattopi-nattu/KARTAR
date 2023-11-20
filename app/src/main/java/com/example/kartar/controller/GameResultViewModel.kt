package com.example.kartar.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.example.kartar.GameResultActivity
import com.example.kartar.MainActivity
import com.example.kartar.controller.singleton.FirebaseSingleton
import com.example.kartar.model.KARTAData

class GameResultViewModel: ViewModel() {
    val rankingList = mutableStateOf(listOf(GameResultActivity.RankingData()))
    val roomUid = mutableStateOf("")

    /**ランキングデータを取得しリストを作成する**/
    fun getRankingData(context: Context) {
        try {
            Log.d("ranking_uid", roomUid.value)
            val rankingRef = FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/point")
            rankingRef.get().addOnSuccessListener { dataSnapshot ->
                val currentRanking = mutableListOf<GameResultActivity.RankingData>()

                Log.d("ranking_child_first", dataSnapshot.toString())
                dataSnapshot.children.forEach { child ->
                    val playerUid = child.key.toString()
                    val point: Int = child.value.toString().toInt()
                    /*uidからプレイヤー情報を取得*/
                    FirebaseSingleton.firestoreReference.collection("users").document(playerUid).get()
                        .addOnSuccessListener { userInfo ->
                            val userIconUri = userInfo.get("iconImage").toString().toUri()
                            val userName = userInfo.get("userName").toString()
                            currentRanking.add(
                                GameResultActivity.RankingData(
                                    name = userName,
                                    iconUri = userIconUri,
                                    point = point
                                )
                            )
                            rankingList.value = currentRanking.sortedByDescending { it.point }
                            Log.d("ranking", currentRanking.toString())
                            Log.d("ranking", rankingList.value.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.d("ranking_e", e.toString())
                            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
            }
                .addOnFailureListener { e ->
                    Log.d("ranking_e", e.toString())
                    Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.d("ranking_error", e.message.toString())
        }
    }

    /**ResultCheckedButtonを押したときの処理**/
    fun onResultCheckedButtonClick(context: Context) {
        FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player/${FirebaseSingleton.currentUid()}")
            .setValue("checked")
            /*最後に結果確認し終わった人がルームを削除する*/
            .addOnSuccessListener {
                FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}/player").get()
                    .addOnSuccessListener { playerData ->
                        val allChecked = playerData.children.all {
                            it.getValue(String::class.java) == "checked"
                        }
                        if (allChecked) FirebaseSingleton.databaseReference.getReference("room/${roomUid.value}").setValue(null)
                    }
            }
        /*Main画面に移動*/
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        //intent.putExtra("NAVIGATE_TO", MainActivity.Screen.RoomList.route)
        context.startActivity(intent)
    }
}