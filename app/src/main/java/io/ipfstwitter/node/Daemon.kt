package io.ipfstwitter.node

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_MIN
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color.parseColor
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import io.ipfstwitter.node.ui.WebActivity
import io.ipfstwitter.node.utils.MessageEvent
import io.ipfstwitter.node.utils.MessageType
import org.greenrobot.eventbus.EventBus

class DaemonService : Service() {

    override fun onBind(intent: Intent) = null

    companion object {
        var daemon: Process? = null
        var logs: MutableList<String> = mutableListOf()
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannel("ipfsTwitter", "ipfsTwitter", IMPORTANCE_MIN).apply {
                description = "ipfsTwitter"
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(this)
            }

        install()
        start()
        startForeground(1, notification.build())
    }

    fun install() {

        val type = "ipfs"

        bin.apply {
            delete()
            createNewFile()
        }

        val input = assets.open(type)
        val output = bin.outputStream()

        try {
            input.copyTo(output)
        } finally {
            input.close()
            output.close()
        }

        bin.setExecutable(true)
        println("Installed binary")
    }

    fun start() {
        logs.clear()


        exec("").apply {
            daemon = this
            read {
                logs.add(it)
                Log.i("ipfs",it) }
        }
        val handler = Handler()
        handler.postDelayed({ EventBus.getDefault().postSticky(MessageEvent(MessageType.IpfsOk)) }, 3000)

        Log.i("====", logs.toString())
    }


    fun stop() {
        daemon?.destroy()
        daemon = null
    }

    val notificationBuilder = NotificationCompat.Builder(this, "ipfsTwitter")

    val notification
        @SuppressLint("RestrictedApi")
        get() = notificationBuilder.apply {
            mActions.clear()
            setOngoing(true)
            setOnlyAlertOnce(true)
            color = parseColor("#69c4cd")
            setSmallIcon(R.drawable.ic_cloud)
            setShowWhen(false)
            setContentTitle("ipfsTwitter")

            val open = pendingActivity<WebActivity>()
            setContentIntent(open)
            addAction(R.drawable.ic_cloud, "Open", open)

            if (daemon == null) {
                setContentText("ipfsTwitter is not running")

                val start = pendingService(intent<DaemonService>().action("start"))
                addAction(R.drawable.ic_cloud, "start", start)
            } else {
                setContentText("ipfsTwitter is running")

                val restart = pendingService(intent<DaemonService>().action("restart"))
                addAction(R.drawable.ic_cloud, "restart", restart)

                val stop = pendingService(intent<DaemonService>().action("stop"))
                addAction(R.drawable.ic_cloud, "stop", stop)
            }

            val exit = pendingService(intent<DaemonService>().action("exit"))
            addAction(R.drawable.ic_cloud, "exit", exit)
        }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY.also {
        super.onStartCommand(i, f, id)
        when (i?.action) {
            "start" -> start()
            "stop" -> stop()
            "restart" -> {
                stop(); start()
            }
            "exit" -> System.exit(0)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification.build())
    }

}
