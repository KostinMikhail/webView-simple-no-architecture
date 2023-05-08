package com.template

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject
import java.util.UUID

class LoadingActivity : AppCompatActivity() {


    private lateinit var progressBar: ProgressBar
    var finalUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        progressBar = findViewById(R.id.progress_bar)

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val firstLaunch = sharedPreferences.getBoolean("app_first_launch", true)
        val sharedPreferencesFirebaseUrl = sharedPreferences.getString("firebase_url", "")

        if (isNetworkAvailable()) {
            if (firstLaunch) {
                val editor = sharedPreferences.edit()

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
                    val packageName = applicationContext.packageName
                    val userId = UUID.randomUUID().toString()
                    val timeZone = java.util.TimeZone.getDefault().id
                    val utmParameters = "utm_source=google-play&amp;utm_medium=organic"

                    finalUrl = "$firebaseUrl/?packageid=$packageName&userId=$userId&getz=$timeZone&$utmParameters"

                    if (sharedPreferences.getString("firebase_url", "").isNullOrEmpty()) {
                        openMainActivity()
                        Toast.makeText(this, "линки небыло 1 запуск", Toast.LENGTH_SHORT).show()

                    } else {
                       // sharedPreferences.getString("firebase_url", "")?.let { openWebActivity(it) }
                        openWebActivity(firebaseUrl)
                        Toast.makeText(
                            this,
                            "линка была, 1 запуск, открыли вебактивити",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } else {
                    openWebActivity(sharedPreferencesFirebaseUrl)
                    Toast.makeText(this, "1 запуск, линк", Toast.LENGTH_SHORT).show()

                }
                editor.putBoolean("app_first_launch", false)
                editor.apply()

                //если 2 запуск
            } else {
                if (sharedPreferencesFirebaseUrl.isNullOrEmpty()) {
                    openMainActivity()
                    Toast.makeText(
                        this,
                        "2 запуск, линки небыло, открываем мэйн",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    openWebActivity(sharedPreferencesFirebaseUrl)
                    Toast.makeText(
                        this,
                        "2 запуск линка была, открываем веб",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            openMainActivity()
            Toast.makeText(
                this,
                "Нет подключения к интернету, открываем мэйн, флаг app_first_launch не меняем",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openWebActivity(finalUrl : String) {
        val intent = Intent(this, WebActivity::class.java)
        intent.putExtra("url", finalUrl)
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
                return true
            }
        }
        return false
    }


}