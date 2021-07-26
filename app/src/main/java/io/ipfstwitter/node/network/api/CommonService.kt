package com.coolweather.coolweatherjetpack.data.network.api

import io.ipfstwitter.node.data.VersionConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CommonService {
    @GET("io.ipfstwitter.node/app.update.json")
    fun loadVersion(@Query("v") v:Long): Call<VersionConfig>

}