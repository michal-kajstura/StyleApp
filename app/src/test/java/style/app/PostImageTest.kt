package style.app

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.source
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import style.app.model.Photo
import style.app.network.PhotoSender
import style.app.network.WrongHostnameException
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.test.assertFailsWith


@RunWith(RobolectricTestRunner::class)
class PostImageTest {

    private val server = MockWebServer()
    private val mockContentResolver = Mockito.mock(ContentResolver::class.java)

    @Before
    fun setupPhoto() {
        server.start()
    }

    private fun getTestInputStream(): InputStream {
        val imageUrl = URL("https://homepages.cae.wisc.edu/~ece533/images/airplane.png")
        return imageUrl.openConnection().getInputStream()
    }

    @Test
    fun sendPhoto_valid() {
        val file = File("test.jpg")
        val uri = Uri.fromFile(file)

        `when`(mockContentResolver.openInputStream(uri))
            .thenReturn(getTestInputStream())

        val buffer = Buffer()
        file.source().use {
            buffer.writeAll(it)
        }

        val res = MockResponse()
            .addHeader("Content-Type:image/jpg")
            .setBody(buffer)
        server.enqueue(res)

        val url = server.url("/test/")
        val sender = PhotoSender(mockContentResolver, url)

        val photo = Photo(uri)
        val stylePhoto = Photo(uri)
        val result = sender.send(photo, stylePhoto, true)

        Assert.assertEquals(
            result.byteCount,
            BitmapFactory.decodeStream(getTestInputStream())
                .byteCount
        )
    }

    @Test
    fun sendPhoto_wrongUrl() {
        val file = File("test.jpg")
        val uri = Uri.fromFile(file)

        `when`(mockContentResolver.openInputStream(uri))
            .thenReturn(getTestInputStream())

        val buffer = Buffer()
        file.source().use {
            buffer.writeAll(it)
        }

        val res = MockResponse()
            .addHeader("Content-Type:image/jpg")
            .setBody(buffer)
        server.enqueue(res)

        val wrongUrl = HttpUrl.Builder()
            .scheme("http")
            .host("wrong")
            .build()

        val sender = PhotoSender(mockContentResolver, wrongUrl)

        val photo = Photo(uri)
        val stylePhoto = Photo(uri)

        assertFailsWith<WrongHostnameException> {
            sender.send(photo, stylePhoto, true)
        }
    }

    @After
    fun cleanup() {
        server.shutdown()
    }



}
