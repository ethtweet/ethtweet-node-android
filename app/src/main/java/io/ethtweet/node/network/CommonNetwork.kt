package com.coolweather.coolweatherjetpack.data.network

import com.coolweather.coolweatherjetpack.data.network.api.CommonService
import io.ethtweet.node.network.ServiceCreator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CommonNetwork {

    private val commonService = ServiceCreator.create(CommonService::class.java)

    suspend fun getVersion() = commonService.loadVersion(System.currentTimeMillis()).await()


    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }
            })
        }
    }

    companion object {

        private var network: CommonNetwork? = null

        fun getInstance(): CommonNetwork {
            if (network == null) {
                synchronized(CommonNetwork::class.java) {
                    if (network == null) {
                        network = CommonNetwork()
                    }
                }
            }
            return network!!
        }

    }

}