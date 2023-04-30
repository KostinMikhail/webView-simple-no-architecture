package com.template

import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {
    private lateinit var webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webview = findViewById(R.id.web_view)

        // получаем адрес подписки из настроек приложения
        val url = getPreferences(Context.MODE_PRIVATE).getString("domain", "")

        // если адрес отсутствует или пустой, завершаем активити
        if (url.isNullOrEmpty()) {
            finish()
        } else {
            // настройка параметров WebView
            webview.settings.javaScriptEnabled = true
            webview.settings.domStorageEnabled = true
            CookieManager.getInstance().setAcceptCookie(true)

            // настройка WebViewClient для перехвата переходов по ссылкам
            webview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        view?.loadUrl(url)
                    }
                    return true
                }
            }

            // загрузка страницы подписки
            webview.loadUrl(url)
        }
    }

    // переопределение обработки кнопки "назад" для WebView
    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
}