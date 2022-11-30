package io.ethtweet.node

import android.app.Application
import io.ethtweet.node.apkUpdater.ApkUpdater

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApkUpdater.init(this, "$packageName.fileProvider")
    }

}