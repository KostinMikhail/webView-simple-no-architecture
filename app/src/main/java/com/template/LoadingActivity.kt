package com.template

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Scanner

class LoadingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        progressBar = findViewById(R.id.progress_bar)

        // Инициализируем FirebaseApp
//        FirebaseApp.initializeApp(this)
//
        remoteConfig = FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }


        val inputStream = resources.openRawResource(R.raw.google_services)
        val json = convertStreamToString(inputStream)
        val jsonObject = JSONObject(json)
//        val url: String = jsonObject.getString("firebase_url")

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()



        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUrl = remoteConfig.getString("firebase_url")

                    if (firebaseUrl.isNotEmpty()) {
                        // здесь откройте вашу Activity, которая отображает URL
                        openWebActivity(firebaseUrl)
                        Toast.makeText(this, firebaseUrl, Toast.LENGTH_SHORT).show()
                    } else {
                        // параметра нет на сервере Firebase, открываем MainActivity
                        openMainActivity()
                    }
                } else {
                    // произошла ошибка загрузки параметров, откройте MainActivity
                    openMainActivity()
                }
            }













//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(0)
//            .build()
//
//        val storageRef =
//            FirebaseStorage.getInstance().reference
//                .child("firebase_url")
//
//        storageRef.downloadUrl.addOnSuccessListener { uri ->
//            // uri получен успешно. Загрузите файл JSON из этого URL.
//        }.addOnFailureListener {
//            // Произошла ошибка. Обработайте ее здесь.
//        }
//
//        // применяем настройки конфигурации
//        remoteConfig.setConfigSettingsAsync(configSettings)
//
//        // установка значений по умолчанию
//        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
//
//        // выполнение обновления переменных на сервере Firebase
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener(this) { task ->
//
//                // получение значения check_link - адреса проверки подписки
//                val checkLink = remoteConfig.getString("check_link")
//
//                if (checkLink.isNullOrEmpty()) {
//                    openMainActivity()
//                } else {
//                    // отправка http-запроса для проверки подписки
//                    val userAgent = System.getProperty("http.agent")
//                    val link = "$checkLink&ua=$userAgent"
//                    val client = OkHttpClient()
//                    val request = okhttp3.Request.Builder()
//                        .url(link)
//                        .build()
//
//                    client.newCall(request).enqueue(object : Callback {
//                        override fun onResponse(call: Call, response: Response) {
//                            if (response.isSuccessful) {
//                                val domain = response.body?.string()
//                                if (domain?.isNotEmpty() == true) {
//                                    // сохранение домена подписки в настройках приложения
//                                    saveDomain(domain)
//                                    // переход на страницу подписки
//                                    openWebActivity(domain)
//                                    return
//                                }
//                            }
//                            openMainActivity()
//                        }
//
//                        override fun onFailure(call: Call, e: IOException) {
//                            openMainActivity()
//                        }
//                    })
//                }
//            }

    }

    // открытие главной активности приложения
    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // открытие веб-страницы в кастомной вкладке
    private fun openWebActivity(url: String) {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(this, Uri.parse(url))
        finish()
    }

    private fun convertStreamToString(inputStream: InputStream): String {
        val scanner = Scanner(inputStream, "UTF-8").useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    // сохранение домена подписки в настройках приложения
    private fun saveDomain(url: String) {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putString("domain", url).apply()
    }

    // отслеживание прогресса скачивания
    @SuppressLint("Range")
    private fun observeDownloadProgress(downloadId: Long) {
        val query = DownloadManager.Query().apply {
            setFilterById(downloadId)
        }

        Thread(Runnable {
            var downloading = true
            while (downloading) {
                // получение курсора для запроса загрузки
                val cursor =
                    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(query)
                cursor.moveToFirst()

                // получение информации о состоянии загрузки
                val downloadedBytes =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalBytes =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                // определение, завершена ли загрузка
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }

                // вычисление текущего прогресса загрузки и обновление прогрессбара
                val progress = (downloadedBytes * 100f / totalBytes).toInt()
                runOnUiThread {
                    progressBar.progress = progress
                }

                cursor.close()
            }

            // do something after download complete
            openMainActivity()

        }).start()
    }
}