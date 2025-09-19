package com.dc.markers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable

class ForcedSizeDrawable(
    private val bitmap: Bitmap,
    private val forcedWidth: Int,
    private val forcedHeight: Int
) : BitmapDrawable(null, bitmap) {

    override fun getIntrinsicWidth(): Int = forcedWidth
    override fun getIntrinsicHeight(): Int = forcedHeight

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, bounds, null)
    }
}
