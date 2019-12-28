import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

val arr = byteArrayOf(0x45, 0x19, 0x01, 0x00, 0x00, 0x00)
val wrapped = ByteBuffer.wrap(arr).order(LITTLE_ENDIAN)

println(wrapped.int)