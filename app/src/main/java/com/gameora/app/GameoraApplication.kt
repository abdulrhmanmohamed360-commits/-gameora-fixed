package com.gameora.app

import android.app.Application
import com.google.firebase.FirebaseApp

class GameoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // تهيئة Firebase عند تشغيل التطبيق
        FirebaseApp.initializeApp(this)
    }
}