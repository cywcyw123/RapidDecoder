package rapiddecoder

import android.graphics.Bitmap

internal class BitmapResourceFullDecoder(source: BitmapSource) : BitmapResourceDecoder(source) {
    private var densityScaledWidth = INVALID_SIZE
    private var densityScaledHeight = INVALID_SIZE

    override val width: Int
        get() {
            if (densityScaledWidth == INVALID_SIZE) {
                densityScaledWidth = Math.ceil(sourceWidth.toDouble() * densityRatio).toInt()
            }
            return densityScaledWidth
        }

    override val height: Int
        get() {
            if (densityScaledHeight == INVALID_SIZE) {
                densityScaledHeight = Math.ceil(sourceHeight.toDouble() * densityRatio).toInt()
            }
            return densityScaledHeight
        }

    override fun region(left: Int, top: Int, right: Int, bottom: Int): BitmapLoader {
        if (hasSize && left <= 0 && top <= 0 && right >= width && bottom >= height) {
            return this
        }
        return BitmapResourceRegionDecoder(source, left, top, right, bottom)
    }

    override fun decode(state: BitmapDecodeState): Bitmap {
        state.prepareDecode()
        val opts = state.options
        val bitmap = source.decode(opts) ?: throw DecodeFailedException()

        if (!boundsDecoded) {
            imageMimeType = opts.outMimeType
            val scale = if (source.densityRatioSupported &&
                    opts.inTargetDensity != 0 && opts.inDensity != 0) {
                opts.inTargetDensity.toDouble() / opts.inDensity
            } else {
                1.0
            }
            bitmapDensityRatio = scale.toFloat()
            if (opts.inScaled && opts.inTargetDensity != 0 && opts.inDensity != 0) {
                bitmapWidth = Math.floor(opts.outWidth / scale).toInt()
                bitmapHeight = Math.floor(opts.outHeight / scale).toInt()
                densityScaledWidth = opts.outWidth
                densityScaledHeight = opts.outHeight
            } else {
                bitmapWidth = opts.outWidth
                bitmapHeight = opts.outHeight
                densityScaledWidth = Math.ceil(opts.outWidth * scale).toInt()
                densityScaledHeight = Math.ceil(opts.outHeight * scale).toInt()
            }
            boundsDecoded = true
        }

        return bitmap
    }
}