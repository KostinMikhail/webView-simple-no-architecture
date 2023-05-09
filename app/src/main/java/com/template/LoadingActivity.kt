package com.template

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class LoadingActivity : AppCompatActivity() {


    private lateinit var progressBar: ProgressBar
    var finalUrl: String = ""
    var urlForWeb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        progressBar = findViewById(R.id.progress_bar)

        // FirebaseApp.initializeApp(applicationContext)
        // FirebaseAnalytics.getInstance(this)

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val firstLaunch = sharedPreferences.getBoolean("app_first_launch", true)
        val sharedPreferencesFirebaseUrl = sharedPreferences.getString("firebase_url", "")
        val sharedPreferencesFinalUrl = sharedPreferences.getString("final_url", "")

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
                    val utmParameters = "utm_source=google-play&utm_medium=organic"

                    finalUrl =
                        "$firebaseUrl/?packageid=$packageName&usserid=$userId&getz=$timeZone&getr=$utmParameters"
                    editor.putString("final_url", finalUrl)
                    editor.apply()

                    val checkLink = remoteConfig.getString("check_link")
                    if (checkLink.isNotBlank() && checkLink.startsWith("http")) {
                        Toast.makeText(this, "ПОЛУЧИЛИ ЕБУЧУЮ ЧЕК ЛИНКУ", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "ОБОСРАЛИСЬ", Toast.LENGTH_SHORT).show()
                    }

                    if (sharedPreferences.getString("firebase_url", "").isNullOrEmpty()) {
                        openMainActivity()
                        //1 запуск, линки небыло, открываем мейн

                    } else {

                        val downloadTask = DownloadTask()
                        downloadTask.execute(checkLink)

                        val defaults: Map<String, Any> = mapOf(
                            "check_link" to finalUrl
                        )
                        remoteConfig.setDefaultsAsync(defaults)
                        val cacheExpiration: Long = 3600
                        remoteConfig.fetch(cacheExpiration).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "ZZZZZZZZZZZZZZZZZZZZZZZZссылка есть, заебок")
                                remoteConfig.activate()

                                // Получаем значение параметра check_link
                                val checkLink = remoteConfig.getString("check_link")
                                Log.d(TAG, "ZZZZZZZZZZZZZZZZZZZZZZZZсобственно, ссылка: $checkLink")

                                // Делаем запрос к серверу по полученной ссылке
                                val url = URL(checkLink)
                                val urlConnection = url.openConnection() as HttpURLConnection
                                try {
                                    val inputStream = urlConnection.inputStream
                                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                                    val response = StringBuilder()
                                    bufferedReader.forEachLine { response.append(it) }
                                    Log.d(TAG, "ZZZZZZZZZZZZZZZZZZZZZZZZОТВЕТ СЕРВЕРА $response")
                                } finally {
                                    urlConnection.disconnect()
                                }
                            } else {
                                Log.e(TAG, "ZZZZZZZZZZZZZZZZZZZZZZZZМЫ ОБОСРАЛИСЬ: ${task.exception}")
                            }
                        }


























                        openWebActivity(urlForWeb)
                        //1 запуск, линка есть, открываем веб
                    }
                } else {
                    openWebActivity(urlForWeb)
                    //1 запуск, линка есть, открываем веб

                }
                editor.putBoolean("app_first_launch", false)
                editor.apply()


                //обработка последующих запусков
            } else {
                //линки небыло, открываем мэйн
                if (sharedPreferencesFirebaseUrl.isNullOrEmpty()) {
                    openMainActivity()
                    //линка была, открываем веб
                } else {
                    openWebActivity(urlForWeb)
                }
            }
            //Нет подключения к интернету, открываем мэйн, флаг app_first_launch не меняем
        } else {
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openWebActivity(urlForWeb: String) {
        val intent = Intent(this, WebActivity::class.java)
        intent.putExtra("url", urlForWeb)
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