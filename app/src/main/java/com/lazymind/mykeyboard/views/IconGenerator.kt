package com.lazymind.mykeyboard.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.lazymind.mykeyboard.R
import com.lazymind.mykeyboard.classes.KeyType

class IconGenerator(val context: Context) {
    private val iconSize:Int
    private val textSize:Float
    private val textPaint:Paint = Paint()

    init {
        this.iconSize = context.resources.getDimension(R.dimen.icon_size).toInt()
        this.textSize = context.resources.getDimension(R.dimen.icon_text_size)

        textPaint.color = Color.BLACK
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize

    }

    fun getIconFor(keyType: KeyType):MutableList<Bitmap>{
        if(keyType == KeyType.CHAR_DIG){
            return createCharDigIcon()
        }
        return mutableListOf()
    }

    private fun createCharDigIcon():MutableList<Bitmap>{
        val list:MutableList<Bitmap> = mutableListOf()

        val bitmapOne = drawText("?123")
        list.add(bitmapOne)

        val bitmapTwo = drawText("ABC")
        list.add(bitmapTwo)

        return list
    }

    private fun drawText(text:String):Bitmap{
        val bitmap = Bitmap.createBitmap(iconSize,iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = Rect()

        val textWidth = textPaint.measureText(text)

        textPaint.getTextBounds(text, 0, text.length, rect)
        val textHeight: Float = (rect.height() - 2 * rect.bottom).toFloat()

        canvas.drawText(
            text,
            0 + (canvas.width - textWidth) / 2,
            canvas.height - (canvas.height - textHeight) / 2,
            textPaint
        )
        return bitmap
    }

}