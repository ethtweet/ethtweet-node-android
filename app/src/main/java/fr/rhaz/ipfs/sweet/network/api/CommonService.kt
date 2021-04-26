package com.coolweather.coolweatherjetpack.data.network.api

import fr.rhaz.ipfs.sweet.data.VersionConfig
import retrofit2.Call
import retrofit2.http.GET

interface CommonService {
    @GET("io.ipfstwitter.node/app.update.json")
    fun loadVersion(): Call<VersionConfig>

}