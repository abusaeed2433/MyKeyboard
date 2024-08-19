package com.lazymind.mykeyboard.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import com.lazymind.mykeyboard.R
import com.lazymind.mykeyboard.classes.KeyType

class IconGenerator(val context: Context) {

    companion object{
        const val STROKE_WIDTH = 6f
    }

    private val iconSize:Int
    private val textSize:Float
    private val paint:Paint = Paint()

    init {
        this.iconSize = context.resources.getDimension(R.dimen.icon_size).toInt()
        this.textSize = context.resources.getDimension(R.dimen.icon_text_size)

        resetPaint()
    }

    private fun resetPaint(){
        paint.reset()

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.textSize = textSize
    }

    fun getIconFor(keyType: KeyType):MutableList<Bitmap>{
        if(keyType == KeyType.CHAR_DIG){
            return createCharDigIcon()
        }

        if(keyType == KeyType.CAPS){
            return createCapsIcon()
        }
        if(keyType == KeyType.SYMBOL_SWITCH){
            return createSymbolSwitchIcon()
        }

        return mutableListOf()
    }

    private fun createSymbolSwitchIcon():MutableList<Bitmap>{

        val bitmapOne = drawText("=\\<")
        val bitmapTwo = drawText("?123")

        return mutableListOf(bitmapOne, bitmapTwo)
    }

    private fun createCharDigIcon():MutableList<Bitmap>{
        val list:MutableList<Bitmap> = mutableListOf()

        val bitmapOne = drawText("?123")
        list.add(bitmapOne)

        val bitmapTwo = drawText("ABC")
        list.add(bitmapTwo)

        return list
    }

    private fun createCapsIcon():MutableList<Bitmap> {
        val bitmapOne = createCapsArrow()
        val bitmapTwo = createFilledArrow()
        val bitmapThree = createFilledArrowWithLine()


//        val bitmapOne = drawText("caps")
//        val bitmapTwo = drawText("Caps")
//        val bitmapThree = drawText("CAPS")

        return mutableListOf(bitmapOne, bitmapTwo, bitmapThree)
    }

    private fun createFilledArrowWithLine():Bitmap{
        resetPaint()
        paint.strokeWidth = STROKE_WIDTH
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.FILL_AND_STROKE

        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val arrowPath = createArrowPath()
        canvas.drawPath(arrowPath, paint)

        val rectPath = createRectPath()
        canvas.drawPath(rectPath, paint)

        return bitmap
    }

    private fun createFilledArrow():Bitmap{
        resetPaint()
        paint.strokeWidth = STROKE_WIDTH
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.FILL_AND_STROKE

        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val path = createArrowPath()
        canvas.drawPath(path, paint)

        canvas.drawLine(calc(.40f),calc(.80f),calc(.60f),calc(0.80f),paint)

        return bitmap
    }

    private fun createCapsArrow():Bitmap{
        resetPaint()
        paint.strokeWidth = STROKE_WIDTH
        paint.strokeCap = Paint.Cap.ROUND

        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val path = createArrowPath()
        canvas.drawPath(path, paint)

        canvas.drawLine(calc(.40f),calc(.80f),calc(.60f),calc(0.80f),paint)

        return bitmap
    }

    private fun createRectPath():Path {
        val path = Path()

        path.moveTo(calc(.40f), calc(.70f)+STROKE_WIDTH)
        path.lineTo(calc(.60f), calc(.70f)+STROKE_WIDTH)
        path.lineTo(calc(.60f), calc(.80f)+STROKE_WIDTH)
        path.lineTo(calc(.40f), calc(.80f)+STROKE_WIDTH)
        path.lineTo(calc(.40f), calc(.70f)+STROKE_WIDTH)

        return path
    }

    private fun createArrowPath():Path {
        val path = Path()
        path.moveTo( calc(.40f), calc(.65f))

        path.lineTo( calc(.40f), calc(.45f))

        path.lineTo( calc(.30f), calc(.45f))
        path.lineTo( calc(.50f), calc(.25f))

        path.lineTo( calc(.70f), calc(.45f))
        path.lineTo( calc(.60f), calc(.45f))

        path.lineTo( calc(.60f), calc(.65f))
        path.lineTo( calc(.40f), calc(.65f))

        return path
    }

    private fun calc(ratio:Float):Float{
        return ratio * iconSize
    }

    private fun drawText(text:String):Bitmap{
        paint.style = Paint.Style.FILL

        val bitmap = Bitmap.createBitmap(iconSize,iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = Rect()

        val textWidth = paint.measureText(text)

        paint.getTextBounds(text, 0, text.length, rect)
        val textHeight: Float = (rect.height() - rect.bottom).toFloat()

        canvas.drawText(
            text,
            0 + (canvas.width - textWidth) / 2,
            canvas.height - (canvas.height - textHeight) / 2,
            paint
        )
        return bitmap
    }

}