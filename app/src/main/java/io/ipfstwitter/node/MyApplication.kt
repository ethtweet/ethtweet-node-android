package io.ipfstwitter.node

import android.app.Application
import io.ipfstwitter.node.apkUpdater.ApkUpdater

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApkUpdater.init(this, "$packageName.fileProvider")
    }

}