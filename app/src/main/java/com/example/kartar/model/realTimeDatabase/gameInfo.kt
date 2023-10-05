package com.example.kartar.model.realTimeDatabase

data class gameInfo(
    var kartaUid: String = "",
    var next: String = "1:00",
    var now: Int = 0,
    var play: Int = 5
)
