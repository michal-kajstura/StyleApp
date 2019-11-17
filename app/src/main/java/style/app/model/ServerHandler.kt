package style.app.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.jcraft.jsch.JSch
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

class ServerHandler {
    private val jsch = JSch()

    class OpenSshTask: AsyncTask<ServerHandler, Void, Unit>() {
        override fun doInBackground(vararg params: ServerHandler?){
            val handler = params[0]
            if (handler != null) {
                val session = handler.jsch.getSession(USERNAME, HOSTNAME, PORT)
                session.setConfig("StrictHostKeyChecking", "no")
                session.setPassword(PASSWORD)
                session.timeout = 100000
                session.connect()
                session.setPortForwardingL(LOCAL_PORT, "localhost", REMOTE_PORT)
            }
        }
    }

    class ReceivePhotoTask: AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg params: Void?): Bitmap {
            val inputStream = getInputStream()
            val buffer = ByteArray(BUFFER_SIZE)
            val imageSize = receiveHeader(inputStream, buffer)
            val imageBytes = ByteArray(imageSize)
            for (i in HEADER_SIZE until BUFFER_SIZE)
                imageBytes[i - HEADER_SIZE] = buffer[i]

            var received = BUFFER_SIZE
            while (received < imageBytes.size) {
                inputStream.read(buffer, 0, BUFFER_SIZE)
                val upTo = if (received + BUFFER_SIZE < imageSize)
                    received + BUFFER_SIZE else imageBytes.size
                for (i in received until upTo) {
                    imageBytes[i] = buffer[i - received]
                }
                println(rceived)
                received += BUFFER_SIZE
            }
            inputStream.close()

            val bitmap = BitmapFactory
                .decodeByteArray(imageBytes, 0, imageBytes.size)
            return bitmap
        }

        private fun receiveHeader(inputStream: InputStream, buffer: ByteArray): Int {
            inputStream.read(buffer, 0, BUFFER_SIZE)
            return getImageSize(buffer)
        }

        private fun getInputStream(): InputStream {
            val socket = Socket(LOCALHOST, LOCAL_PORT)
            return  socket.getInputStream()
        }

        private fun getImageSize(imageBytes: ByteArray): Int {
            val headerBytes = imageBytes.sliceArray(0 until HEADER_SIZE)
            val wrapped = ByteBuffer.wrap(
                headerBytes
            ).order(ByteOrder.LITTLE_ENDIAN)
            return wrapped.int
        }
    }

    class SendPhotoTask: AsyncTask<Photo, Void, Unit>() {
        private val socket = Socket(LOCALHOST, LOCAL_PORT)

        override fun doInBackground(vararg params: Photo?) {
            val photo = params[0]
            val path = photo?.path
            val imageBytes = Files.readAllBytes(Paths.get(path))
            val outputStream = getOutputStream()
            sendHeader(imageBytes, outputStream)
            outputStream.write(imageBytes)
            outputStream.flush()
            socket.close()
        }

        private fun getOutputStream(): OutputStream {
            val os = socket.getOutputStream()
            return BufferedOutputStream(os)
        }

        private fun sendHeader(imageBytes: ByteArray, outputStream: OutputStream) {
            val header = ByteBuffer
                .allocate(HEADER_SIZE)
                .putInt(imageBytes.size)
                .array()
            outputStream.write(header)
        }

    }

    init {
        establishConnection()
    }

    private fun establishConnection() {
        OpenSshTask().execute(this)

    }

    fun receivePhoto(): Bitmap {
        return ReceivePhotoTask().execute().get()
    }



}