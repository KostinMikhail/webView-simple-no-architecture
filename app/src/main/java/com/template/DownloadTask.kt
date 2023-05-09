package com.template

import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DownloadTask : AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg params: String): String? {
        val url = URL(params[0])
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val inputStream = urlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            bufferedReader.forEachLine { response.append(it) }
            return response.toString()
        } finally {
            urlConnection.disconnect()
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.d(TAG, "Server response: $result")

    }
}
