package style.app.controller

import android.app.Application
import style.app.model.ConnectionHandler

class StartupApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ConnectionHandler().establishConnection()
    }
}