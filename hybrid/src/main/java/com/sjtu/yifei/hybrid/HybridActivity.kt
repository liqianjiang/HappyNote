package com.sjtu.yifei.hybrid

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.view.*
import android.webkit.*
import com.sjtu.yifei.annotation.Route
import com.sjtu.yifei.base.BaseActivity
import com.sjtu.yifei.base.util.FileUtil
import com.sjtu.yifei.base.util.LogUtil
import com.sjtu.yifei.base.util.NetworkUtil
import com.sjtu.yifei.base.util.setupActionBar
import com.sjtu.yifei.hybrid.web.BridgeWebView
import com.sjtu.yifei.hybrid.web.BridgeWebViewClient
import com.sjtu.yifei.router.RouterPath
import kotlinx.android.synthetic.main.hybrid_activity.*
import java.io.File

@Route(path = RouterPath.LAUNCHER_HYBRID)
class HybridActivity : BaseActivity() {

    companion object {
        const val EXTRA_SEARCH_KEY = "url"
    }

    private lateinit var webView: BridgeWebView
    private var mUrl: String = ""

    private var currentTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentTime = System.currentTimeMillis()

        setContentView(R.layout.hybrid_activity)
        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        mUrl = intent.getStringExtra(EXTRA_SEARCH_KEY)

        webView = BridgeWebView(applicationContext)
        webView_root.addView(webView,0,ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        initWebSettings()
        initWebViewClient()
        initWebChromeClient()
        if (!TextUtils.isEmpty(mUrl)) {
            if (mUrl.startsWith("www.")) {
                mUrl = "https://$mUrl"
            }
            webView.loadUrl(mUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        webView.removeJavascriptInterface("searchBoxJavaBridge_")
        webView.removeJavascriptInterface("accessibilityTraversal")
        webView.removeJavascriptInterface("accessibility")
        val settings = webView.settings
        //支持JS
        settings.javaScriptEnabled = true
        //设置适应屏幕
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        //支持缩放
        settings.setSupportZoom(false)
        //隐藏原生的缩放控件
        settings.displayZoomControls = false
        //支持内容重新布局
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.supportMultipleWindows()
        settings.setSupportMultipleWindows(true)
        //设置缓存模式
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCacheEnabled(true)
        val cacheDir = FileUtil.getCacheDir(webView.context)
        settings.setAppCachePath("$cacheDir${File.separator}webAppCache")

        LogUtil.e(TAG, "WebView file path is $cacheDir${File.separator}webAppCache")

        //设置可访问文件
        settings.allowFileAccess = true

        //支持获取手势焦点
        webView.requestFocusFromTouch()
        //当webview调用requestFocus时为webview设置节点
        settings.setNeedInitialFocus(true)
        //支持自动加载图片
        settings.loadsImagesAutomatically = Build.VERSION.SDK_INT >= 19
        settings.setNeedInitialFocus(true)
        //设置编码格式
        settings.defaultTextEncodingName = "UTF-8"

        if (NetworkUtil.isNetworkAvailable(this.applicationContext)) {
            settings.cacheMode = WebSettings.LOAD_DEFAULT
        } else {
            settings.cacheMode = WebSettings.LOAD_CACHE_ONLY
        }

        /* if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             WebView.setWebContentsDebuggingEnabled(true)
         }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }*/
    }

    private fun initWebViewClient() {
        webView.webViewClient = object : BridgeWebViewClient(webView) {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentTime = System.currentTimeMillis()
                progressBar.visibility = View.VISIBLE
            }

            override fun onCustomPageFinished(view: WebView?, url: String?) {
                super.onCustomPageFinished(view, url)
                progressBar.progress = 100
                progressBar.visibility = View.GONE
                LogUtil.d(TAG, "HybridActivity load web view cost:${System.currentTimeMillis() - currentTime}")
            }

            override fun onCustomShouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                LogUtil.e(TAG, "onReceivedError: ${error?.errorCode} -> ${error?.description}")
            }

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                LogUtil.e(TAG, "onReceivedHttpError: ${errorResponse?.statusCode} -> ${errorResponse?.reasonPhrase}")
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
                LogUtil.e(TAG, "onReceivedSslError: ${error?.toString()}")
            }
        }
    }

    private fun initWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress - 5
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                setTitle(title)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.hybrid_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.hybrid_menu_refresh -> {
                webView.reload()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView_root.removeView(webView)
        webView.removeAllViews()
        webView.destroy()
    }
}
