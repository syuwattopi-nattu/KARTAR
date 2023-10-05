package com.example.kartar.model

data class RoomData @JvmOverloads constructor(
    var name: String = "ルーム",
    var count: Int = 1,
    var isStart: String = "false",
    var kind: String = "public",
    var roomUid: String = ""
)
