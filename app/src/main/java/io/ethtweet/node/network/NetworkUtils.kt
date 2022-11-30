package io.ethtweet.node.network

import android.util.Log
import com.coolweather.coolweatherjetpack.data.network.CommonNetwork
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class NetworkUtils {
    fun isHostConnectable(host: String?, port: Int): Boolean {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(host, port))
        } catch (e: IOException) {
            Log.e("====",e.message)
            return false
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e("====",e.message)
            }
        }
        return true
    }
    companion object {

        private var network: NetworkUtils? = null

        fun getInstance(): NetworkUtils {
            if (network == null) {
                synchronized(CommonNetwork::class.java) {
                    if (network == null) {
                        network = NetworkUtils()
                    }
                }
            }
            return network!!
        }

    }

}