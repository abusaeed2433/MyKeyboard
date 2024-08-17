package com.lazymind.mykeyboard.views

import android.graphics.Bitmap
import android.graphics.RectF
import kotlin.math.max

class Layout(
    val noOfRow:Int,
    val items: Array<ArrayList<Item>>,
    val layoutType:LayoutType,
    val layoutListener:LayoutListener
) {

    var isCapsModeOn = false
    val maxWeight:Int
    val rowWeight:IntArray = IntArray(noOfRow){0}

    init {
        var localMax = 0
        for(i in 0 until noOfRow){
            rowWeight[i] = 0
            for(j in 0 until items[i].size){
                rowWeight[i] += items[i][j].weight
            }

            localMax = max(localMax, rowWeight[i])
        }

        maxWeight = localMax
    }

    fun getHolderId(x:Float, y:Float):Item?{
        for(row in  items){
            for(item in row){
                if(item.baseRect.contains(x,y)) {
                    if(item.isCaps()){
                        isCapsModeOn = !isCapsModeOn
                        layoutListener.onRefreshRequest()
                    }
                    return item
                }
            }
        }
        return null
    }

    fun getIconFor(item:Item):Bitmap?{
        if(item.isCaps() && isCapsModeOn){
            return item.getBitmapAtIndex(1)
        }
        return item.getBitmapAtIndex(0)
    }

    fun getItemAt(x:Int, y: Int):Item?{
        if(x < 0 || x >= noOfRow || y < 0 || y >= items[x].size) return null

        return items[x][y]
    }

    interface LayoutListener{
        fun onRefreshRequest()
    }

    enum class LayoutType{ MAIN, SECONDARY }
    enum class KeyType(val x:Int, val y:Int, val weight:Int=1){
        OPTIONS(0,0), STICKER(0,1), GIF(0,2), CLIPBOARD(0,3), THEME(0,4), MIC(0,5),

        CAPS(3,0), BACKSPACE(3,8),

        CHAR_DIG(4,0), LANGUAGE(4,2), SPACE(4,3,5), NEXT(4,5), NORMAL(-1,-1);
    }

    class Item(
        val x:Int, val y:Int, val key:String,
        val weight: Int = 1,
        val pressKey:String? = null,
        val keyType:KeyType,// = KeyType.NORMAL,
        val bitmaps:MutableList<Bitmap> = ArrayList()
    ){
        companion object{
            private const val HOLDER_PAD_WIDTH = .20f // 20%
            private const val HOLDER_PAD_HEIGHT = .25f // 25%
            private const val BASE_GAP = 0.05f // 5%
        }

        val baseRect:RectF = RectF()
        val iconRect:RectF = RectF()
        var textWidth:Float = 0f
        var textHeight:Float = 0f
        val hasIcon:Boolean = bitmaps.isNotEmpty()

        fun updateRectAndIcon(left: Float, top: Float, right: Float, bottom: Float) {
            updateRect(baseRect, left, top, right, bottom, BASE_GAP, BASE_GAP)
            updateRect(iconRect, left, top, right, bottom, HOLDER_PAD_WIDTH, HOLDER_PAD_HEIGHT)
        }

        private fun updateRect(rect: RectF, left: Float, top: Float, right: Float, bottom: Float, padWidth:Float, padHeight:Float) {
            val widthPad = ((right - left)/weight) * padWidth
            val heightPad = (bottom - top) * padHeight
            rect.set(left+widthPad, top+heightPad, right-widthPad, bottom-heightPad)
        }

        fun getBitmapAtIndex(index:Int):Bitmap?{
            if(index < 0 || index >= bitmaps.size) return null
            return bitmaps[index]
        }

        fun isBackSpace():Boolean{  return (x == KeyType.BACKSPACE.x && y == KeyType.BACKSPACE.y)  }
        fun isSpace():Boolean{  return (x == KeyType.SPACE.x && y == KeyType.SPACE.y)  }
        fun isCaps():Boolean{  return (x == KeyType.CAPS.x && y == KeyType.CAPS.y)  }
        fun isNext():Boolean{  return (x == KeyType.NEXT.x && y == KeyType.NEXT.y)  }
    }
}
