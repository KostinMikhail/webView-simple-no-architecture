package com.template

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

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

        val storageRef =
            FirebaseStorage.getInstance().getReference()
                .child("C/Users/kmm/Desktop/google-services.json")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // uri получен успешно. Загрузите файл JSON из этого URL.
        }.addOnFailureListener {
            // Произошла ошибка. Обработайте ее здесь.
        }

        // применяем настройки конфигурации
        remoteConfig.setConfigSettingsAsync(configSettings)

        // установка значений по умолчанию
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // выполнение обновления переменных на сервере Firebase
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->

                // получение значения check_link - адреса проверки подписки
                val checkLink = remoteConfig.getString("check_link")

                if (checkLink.isNullOrEmpty()) {
                    openMainActivity()
                } else {
                    // отправка http-запроса для проверки подписки
                    val userAgent = System.getProperty("http.agent")
                    val link = "$checkLink&ua=$userAgent"
                    val client = OkHttpClient()
                    val request = DownloadManager.Request(Uri.parse(link))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setAllowedOverMetered(true)
                        .setAllowedNetworkTypes(
                            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                        )
                        .setTitle("Downloading")
                        .setDescription("Please wait...")
                        .setDestinationInExternalFilesDir(
                            applicationContext,
                            "/",
                            " "
                        )

                    // выполнение запроса и получение идентификатора загрузки
                    val downloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val downloadId = downloadManager.enqueue(request)

                    // следим за прогрессом загрузки
                    observeDownloadProgress(downloadId)
                }
            }

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