package rapiddecoder.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import rapiddecoder.RewindableInputStream
import java.io.InputStream

internal class InputStreamBitmapSource(stream: InputStream) : BitmapSource {
    private val wrappedStream = RewindableInputStream(stream)

    override val reopenable: Boolean
        get() = false

    override val supportsDensityScale: Boolean
        get() = false

    override fun decode(opts: BitmapFactory.Options?): Bitmap? {
        if (opts?.inJustDecodeBounds != true) {
            wrappedStream.rewind()
        }
        return BitmapFactory.decodeStream(wrappedStream, null, opts)
    }

    override fun createRegionDecoder(): BitmapRegionDecoder {
        wrappedStream.rewind()
        return BitmapRegionDecoder.newInstance(wrappedStream, false)
    }
}