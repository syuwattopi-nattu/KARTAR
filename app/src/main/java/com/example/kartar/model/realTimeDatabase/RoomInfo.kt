package com.example.kartar.model.realTimeDatabase

data class RoomInfo @JvmOverloads constructor(
    var start: String = "false",
    var count: Int = 1,
    var kind: String = "public",
    var roomName: String = "ルーム"
)
