package style.app.network

import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import style.app.NGROK_ADDRESS
import style.app.TIMEOUT
import java.util.concurrent.TimeUnit


object ConnectionHandler {
    val httpClient = initOkHttpClient()

    private var serverAddress = ""

    fun isConnected(): Boolean {
        val requestUrl = getUrl("test")
        val postBody = MultipartBody.Builder()
           .setType(MultipartBody.FORM)
           .addFormDataPart("test", "test").build()

        val request = Request.Builder()
            .url(requestUrl)
            .post(postBody)
            .build()

        val response = httpClient
            .newCall(request)
            .execute()

        return (response.code() == 200 || response.code() == 308)
    }

    private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun setNgrokSuffix(ngrokSuffix: String) {
        serverAddress = "$ngrokSuffix.$NGROK_ADDRESS"
    }

    fun getUrl(url: String=""): HttpUrl {
        return HttpUrl.Builder()
            .scheme("http")
            .host(serverAddress)
            .addPathSegment(url)
            .build()
    }
}

