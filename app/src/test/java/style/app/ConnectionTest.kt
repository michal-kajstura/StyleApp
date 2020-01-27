package style.app


import android.content.ContentResolver
import android.net.Uri
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import style.app.model.Photo
import style.app.network.ConnectionHandler
import style.app.network.PhotoSender
import java.io.File
import java.io.InputStream
import java.net.URL


@RunWith(RobolectricTestRunner::class)
class ConnectionTest{
    private val TEST_ITERATIONS = 10
    private val MAX_TIME_IN_MS = 2 * 1000

    private val mockContentResolver = Mockito.mock(ContentResolver::class.java)

    private fun getTestInputStream(): InputStream {
        val imageUrl = URL("https://upload.wikimedia.org/wikipedia/commons/d/db/Patern_test.jpg")
        return imageUrl.openConnection().getInputStream()
    }

    @Test
    fun sendPhoto_time() {
//        val file = File("in2.jpg")
        val file = File("van-gogh-sunflowers.jpg")
        val uri = Uri.fromFile(file)

        `when`(mockContentResolver.openInputStream(uri))
            .thenReturn(getTestInputStream())

        ConnectionHandler.setNgrokSuffix(TEST_SUFFIX)
        val sender = PhotoSender(mockContentResolver, ConnectionHandler.getUrl("transfer"))

        val photo = Photo(uri)
        val stylePhoto = Photo(uri)

        var timeElapsed = 0L
        for (i in 1..TEST_ITERATIONS) {
            val startTime = System.currentTimeMillis()
            sender.send(photo, stylePhoto, true)
            val endTime = System.currentTimeMillis()
            timeElapsed += endTime - startTime
        }

        val avgTime=  timeElapsed.toDouble() / TEST_ITERATIONS

        Assert.assertTrue(avgTime < MAX_TIME_IN_MS)
    }

}
