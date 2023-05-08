package com.template

import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webView = findViewById(R.id.web_view)

        // Получаем ссылку из интента, который запустил WebActivity
        val url = intent.getStringExtra("url")

        // Отображаем ссылку в WebView
        webView.loadUrl(url ?: "https://www.google.com/")
    }
}





//    private lateinit var webview: WebView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_web)
//        webview = findViewById(R.id.web_view)
//
//        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
//        val url = sharedPreferences.getString("firebase_url", "")
//
//        if (url.isNullOrEmpty()) {
//            finish()
//        } else {
//            webview.settings.javaScriptEnabled = true
//            webview.settings.domStorageEnabled = true
//            CookieManager.getInstance().setAcceptCookie(true)
//
//            webview.webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                    if (url != null) {
//                        view?.loadUrl(url)
//                    }
//                    return true
//                }
//            }
//
//            webview.loadUrl(url)
//        }
//    }
//
//    override fun onBackPressed() {
//        if (webview.canGoBack()) {
//            webview.goBack()
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    private fun saveDomainToSharedPreferences(domain: String) {
//        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("domain", domain)
//        editor.commit() // добавляем эту строку
//    }
//}