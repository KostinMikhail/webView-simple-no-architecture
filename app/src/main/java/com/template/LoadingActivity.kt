package com.template

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject

class LoadingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        progressBar = findViewById(R.id.progress_bar)

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val firebaseUrl = sharedPreferences.getString("firebase_url", "")
        if (firebaseUrl.isNullOrEmpty()) {
            openMainActivity()
            // Сохраняем флаг состояния приложения
            sharedPreferences.edit { putBoolean("app_opened", true) }
        } else {

        val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        val resourceId = resources.getIdentifier("google_services", "raw", packageName)
        val localJson = resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        val localData = JSONObject(localJson)
        val projectInfo = localData.getJSONObject("project_info")
        val firebaseUrl = projectInfo.getString("firebase_url")
        remoteConfig.setDefaultsAsync(jsonToMap(localData))
            if (firebaseUrl.isNullOrEmpty()) {
                openMainActivity()
            } else {
                openWebActivity(firebaseUrl)
            }


        Toast.makeText(this, firebaseUrl, Toast.LENGTH_SHORT).show()
    }
    }
    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openWebActivity(url: String) {
        val intent = Intent(this, WebActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
        finish()
    }

    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value: Any = json.get(key)
            if (value is JSONObject) {
                value = jsonToMap(value)
            }
            map[key] = value
        }
        return map
    }
}