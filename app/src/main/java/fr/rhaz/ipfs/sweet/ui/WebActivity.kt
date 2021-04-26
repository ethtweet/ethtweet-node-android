package fr.rhaz.ipfs.sweet.ui

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kelin.apkUpdater.ApkUpdater
import com.kelin.apkUpdater.SignatureType
import com.kelin.apkUpdater.UpdateInfoImpl
import com.kelin.apkUpdater.UpdateType
import com.kelin.apkUpdater.callback.IUpdateCallback
import fr.rhaz.ipfs.sweet.*
import fr.rhaz.ipfs.sweet.data.VersionConfig
import fr.rhaz.ipfs.sweet.utils.MessageEvent
import fr.rhaz.ipfs.sweet.utils.MessageType
import fr.rhaz.ipfs.sweet.utils.StatusBarUtil
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.activity_web.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class WebActivity : AppCompatActivity(), AdvancedWebView.Listener {

    var url:String = "https://ipfs.io/ipfs/QmVHVgFoj1zSReUMQgfoWzAwdietrTrEVifdynQ5ZRkQ5R/"


    val viewModel by lazy {
        ViewModelProvider(this).get(WebViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        StatusBarUtil.setStatusColor(this,false,true,R.color.color242A37)
        startService<DaemonService>()

        webview!!.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.setSupportZoom(true)
            settings.defaultTextEncodingName = "utf-8"

            webChromeClient = MyWebChromeClient()
            webViewClient = MyWebViewClient()

            addJavascriptInterface(object {
                @JavascriptInterface
                fun logs() = json(DaemonService.logs).toString()

                @JavascriptInterface
                fun execute(msg: String) {
                    DaemonService.logs.add("> $msg")
                    exec(msg).read { DaemonService.logs.add(it) }
                }
            }, "android")

            loadUrl(url)
        }
        initObserver()
        initData()
    }

    private inner class MyWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            setProgress(newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            if (title.contains("html")) {
                return
            }
        }

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ): Boolean {
            uploadMessageAboveL = filePathCallback
            openImageChooserActivity()
            return true
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
            webview.isVisible = true
            StatusBarUtil.setStatusColor(this@WebActivity,false,true,R.color.colorPrimaryDark)
            ivlogo.isVisible = false
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
        }
    }


    fun initData(){
        webview.isVisible = false;
        viewModel.loadVersion()
        //viewModel.checkIpfs()
    }

    fun initObserver(){
        viewModel.version.observe(this, Observer { versionConfig ->
            versionConfig.let {
                checkUpdate(versionConfig)
            }
        })
        viewModel.ipCheckRst.observe(this, Observer { ipChecked ->
            if (ipChecked)
                webview.loadUrl(url)
        })
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)//注册，重复注册会导致崩溃
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)//解绑
    }


    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.IpfsOk -> {
                //viewModel.checkIpfs()
                Log.d("====", "Ipfs onMessageEvent")
                webview.loadUrl(url)
            }
        }
    }
    override fun onBackPressed() = webview.goBack()

    override fun onPageStarted(url: String?, favicon: Bitmap?) {}

    override fun onPageFinished(url: String?) {}

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {}

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {}



    //////////////////////////////////版本更新 begin////////////////////////////////
    private val apkUpdater: ApkUpdater by lazy {
        ApkUpdater.Builder()
            .setCallback(ApkCompleteUpdateCallback())
            .create()
    }
    private fun checkUpdate(data: VersionConfig) {
        var updateInfo: UpdateInfoImpl = UpdateInfoImpl(
            data.url, //安装包下载地址
            data.version, //网络上的版本号，用于判断是否可以更新(是否大于本地版本号)。
            data.version_name, //版本名称，用于显示在弹窗中，以告知用户将要更到哪个版本。
            UpdateType.UPDATE_FORCE,  //是否是强制更新，如果干参数为true则用户没有进行更新就不能继续使用App。(当旧版本存在严重的Bug时或新功能不与旧版兼容时使用)
            getString(R.string.kelin_apk_updater_sub_title),  //升级弹窗的标题。
            data.msg, //升级弹窗的消息内容，用于告知用户本次更新的内容。
            SignatureType.MD5, //安装包完整性校验开启，并使用MD5进行校验，如果不想开启，传null。(目前只支持MD5和SHA1)
            ""  //完成性校验的具体值，返回空或null则不会进行校验。
        )
        if(data.forcibly == 1){
            updateInfo.updateType = UpdateType.UPDATE_FORCE
        }else{
            updateInfo.updateType = UpdateType.UPDATE_NORMAL
        }
        apkUpdater.check(updateInfo, false)
    }

    private inner class ApkCompleteUpdateCallback : IUpdateCallback {

        override fun onSuccess(
            isAutoCheck: Boolean,
            haveNewVersion: Boolean,
            curVersionName: String,
            updateType: UpdateType
        ) {
            if (!isAutoCheck && !haveNewVersion) {
                //Toast.makeText(applicationContext, "$curVersionName 已是最新版本！", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFiled(
            isAutoCheck: Boolean,
            isCanceled: Boolean,
            haveNewVersion: Boolean,
            curVersionName: String,
            checkMD5failedCount: Int,
            updateType: UpdateType
        ) {
            if (isCanceled) {
                if (!isAutoCheck) {
                    viewModel.version.value.let {
                        //在取消更新、安装后，如果是强制更新，会提示更新
                        if(viewModel.version.value?.forcibly == 1){
                            it?.let { it1 -> checkUpdate(it1) }
                        }
                    }
                }
            } else {
                if (updateType == UpdateType.UPDATE_FORCE) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.kelin_apk_updater_force_tips) + "$curVersionName",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.kelin_apk_updater_fail) + " $curVersionName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun onCompleted() {
        }
    }
    //////////////////////////////////版本更新 end////////////////////////////////



    //////////////////////////////////文件上传 begin////////////////////////////////
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount){
                            i -> clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }
    //////////////////////////////////文件上传 end////////////////////////////////

    companion object {
        private val FILE_CHOOSER_RESULT_CODE = 10000
    }

}
