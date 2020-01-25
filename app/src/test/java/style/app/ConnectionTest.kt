package style.app

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toFile
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import style.app.model.Photo
import style.app.network.ConnectionHandler
import style.app.network.PhotoSender
import java.io.File
import java.io.FileInputStream


@RunWith(RobolectricTestRunner::class)
class ConnectionTest {

    @Test
    fun sendPhoto_connection() {

        val uri = Uri.fromFile(File("test.jpg"))

        val mockContentResolver = Mockito.mock(ContentResolver::class.java)
       `when`(mockContentResolver.openInputStream(uri))
           .thenReturn(FileInputStream(File("test.jpg")))

        ConnectionHandler.establishConnection(16006)
        val sender = PhotoSender(mockContentResolver, ConnectionHandler.httpClient)

        val photo = Photo(uri)
        val stylePhoto = Photo(uri)
        sender.send(photo, stylePhoto, true)
    }
}
