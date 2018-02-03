package rapiddecoder.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder

internal class ByteArrayBitmapSource(
        private val bytes: ByteArray,
        private val offset: Int,
        private val length: Int) : BitmapSource {
    override val reopenable: Boolean
        get() = true

    override val supportsDensityScale: Boolean
        get() = false

    override fun decode(opts: BitmapFactory.Options?): Bitmap? =
            BitmapFactory.decodeByteArray(bytes, offset, length, opts)

    override fun createRegionDecoder(): BitmapRegionDecoder =
            BitmapRegionDecoder.newInstance(bytes, offset, length, false)
}