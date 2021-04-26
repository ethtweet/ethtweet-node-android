package fr.rhaz.ipfs.sweet.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coolweather.coolweatherjetpack.data.network.CommonNetwork
import fr.rhaz.ipfs.sweet.data.VersionConfig
import fr.rhaz.ipfs.sweet.network.NetworkUtils
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
                NetworkUtils.getInstance().isHostConnectable("127.0.0.1",5001)
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