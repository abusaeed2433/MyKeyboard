package com.lazymind.mykeyboard.classes

import android.graphics.Bitmap
import android.graphics.RectF
import kotlin.math.min

// Don't call any function of this class directly. Call via the Row class
class Item( val x:Int, val y:Int, var key:String,
    var weight: Int = 1, var pressKey:String? = null, var keyType: KeyType,// = KeyType.NORMAL,
    var bitmaps:MutableList<Bitmap> = ArrayList()
){
    companion object{
        private const val ICON_PAD_GAP = .20f // 20%
        private const val BORDER_GAP = 0.06f
    }

    val baseRect: RectF = RectF() // used for checking click
    val borderRect: RectF = RectF() // used for drawing
    val iconRect: RectF = RectF() // used for drawing the icon
    var textWidth:Float = 0f
    var textHeight:Float = 0f


    fun updateTextWidthHeight(textWidth:Float, textHeight:Float){
        this.textWidth = textWidth
        this.textHeight = textHeight
    }

    fun hasIcon():Boolean {
        return bitmaps.isNotEmpty()
    }

    fun updateRectAndIcon(left: Float, top: Float, right: Float, bottom: Float) {
        updateBaseRect(left, top, right, bottom)
        updateIconRect(left, top, right, bottom)
    }

    private fun updateBaseRect(left: Float, top: Float, right: Float, bottom: Float){
        val widthPad = ((right - left)/weight) * BORDER_GAP
        val heightPad = (bottom - top) * BORDER_GAP
        borderRect.set(left+widthPad, top+heightPad, right-widthPad, bottom-heightPad)
        baseRect.set(left, top, right, bottom)
    }

    private fun updateIconRect(left: Float, top: Float, right: Float, bottom: Float){
        val width = right - left
        val height = bottom - top

        val minSize = min( width, height ) * (1f - 2f*ICON_PAD_GAP)

        val widthPad = (width - minSize) / 2
        val heightPad = (height - minSize) / 2
        iconRect.set(left+widthPad, top+heightPad, right-widthPad, bottom-heightPad)
    }

    fun getBitmapAtIndex(index:Int): Bitmap?{
        if(index < 0 || index >= bitmaps.size) return null
        return bitmaps[index]
    }

    // Can call these ones
    fun isBackSpace():Boolean{  return (x == KeyType.BACKSPACE.x && y == KeyType.BACKSPACE.y)  }
    fun isSpace():Boolean{  return (x == KeyType.SPACE.x && y == KeyType.SPACE.y)  }
    fun isCaps():Boolean{  return (x == KeyType.CAPS.x && y == KeyType.CAPS.y)  }
    fun isNext():Boolean{  return (x == KeyType.NEXT.x && y == KeyType.NEXT.y)  }
}