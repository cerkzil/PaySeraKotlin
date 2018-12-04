package com.example.android.payserakotlin

import android.content.AsyncTaskLoader
import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class ConvertLoader internal constructor(context: Context, private val mUrl: URL?):AsyncTaskLoader<String>(context) {

    override fun onStartLoading() {
        forceLoad()
    }

    override fun loadInBackground(): String? {
        if (mUrl == null) {
            return null
        }
        try {
            return extractRates(mUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val HTTP_CONNECTION_OKAY = 200
        private val HTTP_READ_TIMEOUT = 10000
        private val HTTP_CONNECT_TIMEOUT = 15000
        @Throws(IOException::class)
        private fun extractRates(url: URL): String? {
            var httpURLConnection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var jSonResponse: String? = null
            try {
                httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.readTimeout = HTTP_READ_TIMEOUT
                httpURLConnection.connectTimeout = HTTP_CONNECT_TIMEOUT
                httpURLConnection.requestMethod = "GET"
                httpURLConnection.connect()
                val responseCode = httpURLConnection.responseCode
                if (responseCode == HTTP_CONNECTION_OKAY) {
                    inputStream = httpURLConnection.inputStream
                    jSonResponse = readStream(inputStream)
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close()

                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect()
                }
            }
            return parseJSON(jSonResponse)
        }

        @Throws(IOException::class)
        private fun readStream(inputStream: InputStream?): String {
            val jSonResponse = StringBuilder()
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
                val reader = BufferedReader(inputStreamReader)
                var line: String? = reader.readLine()
                while (line != null) {
                    jSonResponse.append(line)
                    line = reader.readLine()
                }
            }
            return jSonResponse.toString()
        }

        @Throws(IOException::class)
        private fun parseJSON(jSonResponse: String?): String? {
            var results: String? = null
            try {
                val root = JSONObject(jSonResponse)
                results = root.optString("amount")
            } catch (e: JSONException) {
                Log.e("ConversionLoader", "Problem parsing JSON results", e)
            }
            return results
        }
    }
}



