package style.app.network

import android.os.AsyncTask
import com.jcraft.jsch.JSch
import okhttp3.OkHttpClient
import style.app.*
import java.util.concurrent.TimeUnit

class ConnectionHandler {
    val httpClient = initOkHttpClient()

    class OpenSshTask: AsyncTask<Unit, Void, Unit>() {
        override fun doInBackground(vararg params: Unit?){
            val jsch = JSch()
            val session = jsch.getSession(USERNAME, HOSTNAME, PORT)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setPassword(PASSWORD)
            session.timeout = 100000
            session.connect()
            session.setPortForwardingL(LOCAL_PORT, "localhost", REMOTE_PORT)
        }
    }

    fun establishConnection() {
        OpenSshTask().execute()
    }

     private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
