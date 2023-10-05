package com.example.kartar.controller.singleton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseSingleton {
    /**firestoreのインスタンス**/
    val firestoreReference: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**firebaseAuthのインスタンス**/
    val firebaseAuthReference: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    /**databaseのインスタンス**/
    val databaseReference: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    /**storageのインスタンス**/
    val storageReference: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    /**ログインしていない場合の例外処理**/
    fun ensureLoggedIn() {
        if (currentUid() == null) {
            throw Exception("現在ログインしていないです")
        }
    }

    /**ユーザのuid**/
    fun currentUid(): String? {
        return firebaseAuthReference.currentUser?.uid
    }

    /**ユーザのサインアウト**/
    fun userSignOut() {
        firebaseAuthReference.signOut()
    }
}