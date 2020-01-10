package style.app.network

import android.os.AsyncTask
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import okhttp3.OkHttpClient
import style.app.*
import java.util.concurrent.TimeUnit


object ConnectionHandler {
    val httpClient = initOkHttpClient()
    lateinit var session: Session
    var connected = false

    class OpenSshTask: AsyncTask<Int, Void, Boolean>() {
        override fun doInBackground(vararg params: Int?): Boolean {
            val port = params[0]!!
            val jsch = JSch()
            session = jsch.getSession(USERNAME, HOSTNAME, port)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setPassword(PASSWORD)
            session.timeout = 100000

            try {
                session.connect()
                session.setPortForwardingL(LOCAL_PORT, "localhost", REMOTE_PORT)
                connected = true
                return true
            } catch (e: JSchException) {
                connected = false
                return false
            }
        }
    }

    fun establishConnection(port: Int): Boolean {
        return OpenSshTask().execute(port).get()
    }

    fun deleteConnection() {
        session.delPortForwardingL(LOCAL_PORT)
        connected = false
    }

     private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
