package com.mikal.minorproject

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Created by Mikal Shrestha on 08/07/2024.
 */
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck: FirebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}