package com.example.kartar.model

data class KartaDataFromServer(
    var kartaUid: String,
    var create: String,
    var title: String,
    var description: String,
    var genre: String,
    var kartaImage: String
)
