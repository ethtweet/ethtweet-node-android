package io.ipfstwitter.node.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolweather.coolweatherjetpack.data.network.CommonNetwork
import io.ipfstwitter.node.data.VersionConfig
import io.ipfstwitter.node.network.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebViewModel : ViewModel() {
    var version = MutableLiveData<VersionConfig>()
    var ipCheckRst = MutableLiveData<Boolean>();

    val network by lazy {
        CommonNetwork.getInstance()
    }

    fun loadVersion(){
        launch {
            version.value = network.getVersion()
        }
    }


    fun checkIpfs(){
        launch ({
            val checked = withContext(Dispatchers.IO) {
                NetworkUtils.getInstance().isHostConnectable("127.0.0.1",8080)
            }
            ipCheckRst.value = checked
            Log.d("====","local host is checked:"+checked)

        },{
            ipCheckRst.value = false
            Log.d("====","local host is not ok")
        })

    }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun launch(block: suspend () -> Unit, error: suspend (Throwable) -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
        }
    }

}