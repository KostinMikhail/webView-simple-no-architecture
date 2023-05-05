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
        val sharedPreferencesFirebaseUrl = sharedPreferences.getString("firebase_url", "")

        val editor = sharedPreferences.edit()

        if (sharedPreferences.getBoolean("app_opened", false)) {
            editor.putBoolean("app_opened", true)
            editor.apply()
            if (sharedPreferencesFirebaseUrl.isNullOrEmpty()) {
                val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0)
                    .build()
                remoteConfig.setConfigSettingsAsync(configSettings)
                val resourceId = resources.getIdentifier("google_services", "raw", packageName)
                val localJson =
                    resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
                val localData = JSONObject(localJson)
                val projectInfo = localData.getJSONObject("project_info")
                val firebaseUrl = projectInfo.getString("firebase_url")
                remoteConfig.setDefaultsAsync(jsonToMap(localData))
                editor.putString("firebase_url", firebaseUrl)
                editor.apply()
                // Toast.makeText(this, firebaseUrl, Toast.LENGTH_SHORT).show()
                if (sharedPreferencesFirebaseUrl.isNullOrEmpty()) {
                    openMainActivity()
                    Toast.makeText(this, "линки небыло 1 запуск", Toast.LENGTH_SHORT).show()

                } else {
                    //openWebActivity(firebaseUrl)
                    Toast.makeText(
                        this,
                        "линка появилась 1 запуск, открыли вебактивити",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                //   "firebase_url": "https://test-apk-1-fa3be-default-rtdb.firebaseio.com"
            } else {
                //   openWebActivity(firebaseUrl)
                Toast.makeText(this, "линка была, открываем вебактивити", Toast.LENGTH_SHORT).show()

            }
        } else {
            editor.putBoolean("app_opened", true)
            editor.apply()
            if (sharedPreferencesFirebaseUrl != null) {
                //   openWebActivity(sharedPreferencesFirebaseUrl)
                Toast.makeText(
                    this,
                    "2 запуск, линка есть, открываем вебактивити",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                Toast.makeText(
                    this,
                    "2 запуск, линки небыло, открываем мэйн",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    /*
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
  */


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