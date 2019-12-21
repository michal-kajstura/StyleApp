package style.app.controller

import android.app.Application
import style.app.network.ConnectionHandler

class StartupApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ConnectionHandler().establishConnection()
    }
}