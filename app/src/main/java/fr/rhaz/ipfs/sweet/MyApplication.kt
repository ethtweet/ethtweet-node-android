package fr.rhaz.ipfs.sweet

import android.app.Application
import com.kelin.apkUpdater.ApkUpdater

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApkUpdater.init(this, "$packageName.fileProvider")
    }

}