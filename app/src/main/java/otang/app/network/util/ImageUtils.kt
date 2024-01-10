package otang.app.network.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

object ImageUtils {
    fun createBitmapFromString(speed: String?, unit: String?): Bitmap {
        // Speed
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        // Unit
        val unitPaint = Paint()
        unitPaint.isAntiAlias = true
        unitPaint.textSize = 40f
        unitPaint.textAlign = Paint.Align.CENTER
        unitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        // Speed
        val speedBounds = Rect()
        paint.getTextBounds(speed, 0, speed!!.length, speedBounds)
        // Unit
        val unitBounds = Rect()
        unitPaint.getTextBounds(unit, 0, unit!!.length, unitBounds)
        val width =
            if (speedBounds.width() > unitBounds.width()) speedBounds.width() else unitBounds.width()
        // Create Bitmap
        val bitmap = Bitmap.createBitmap(width, 90, Bitmap.Config.ARGB_8888)
        // Draw Canvas
        val canvas = Canvas(bitmap)
        canvas.drawText(speed, width / 2f, 50f, paint)
        canvas.drawText(unit, width / 2f, 90f, unitPaint)
        return bitmap
    }
}