package com.lazymind.mykeyboard.classes

import android.graphics.Bitmap
import android.graphics.RectF

class Item( val x:Int, val y:Int, var key:String,
    var weight: Int = 1, var pressKey:String? = null, var keyType: KeyType,// = KeyType.NORMAL,
    var bitmaps:MutableList<Bitmap> = ArrayList()
){
    companion object{
        private const val ICON_PAD_WIDTH = .20f // 20%
        private const val ICON_PAD_HEIGHT = .25f // 25%
        private const val BASE_GAP = 0.02f // 5%
    }

    val baseRect: RectF = RectF()
    val iconRect: RectF = RectF()
    var textWidth:Float = 0f
    var textHeight:Float = 0f
    var hasIcon:Boolean = bitmaps.isNotEmpty()

    fun update( key:String = this.key, weight: Int = this.weight, pressKey: String? = this.pressKey,
                keyType: KeyType = this.keyType, bitmaps: MutableList<Bitmap> = this.bitmaps ){
        this.key = key
        this.weight = weight
        this.pressKey = pressKey
        this.keyType = keyType
        this.bitmaps = bitmaps

        hasIcon = bitmaps.isNotEmpty()
    }

    fun updateRectAndIcon(left: Float, top: Float, right: Float, bottom: Float) {
        updateRect(baseRect, left, top, right, bottom, BASE_GAP, BASE_GAP)
        updateRect(iconRect, left, top, right, bottom, ICON_PAD_WIDTH, ICON_PAD_HEIGHT)
    }

    private fun updateRect(rect: RectF, left: Float, top: Float, right: Float, bottom: Float, padWidth:Float, padHeight:Float) {
        val widthPad = ((right - left)/weight) * padWidth
        val heightPad = (bottom - top) * padHeight
        rect.set(left+widthPad, top+heightPad, right-widthPad, bottom-heightPad)
    }

    fun getBitmapAtIndex(index:Int): Bitmap?{
        if(index < 0 || index >= bitmaps.size) return null
        return bitmaps[index]
    }

    fun isBackSpace():Boolean{  return (x == KeyType.BACKSPACE.x && y == KeyType.BACKSPACE.y)  }
    fun isSpace():Boolean{  return (x == KeyType.SPACE.x && y == KeyType.SPACE.y)  }
    fun isCaps():Boolean{  return (x == KeyType.CAPS.x && y == KeyType.CAPS.y)  }
    fun isNext():Boolean{  return (x == KeyType.NEXT.x && y == KeyType.NEXT.y)  }
}