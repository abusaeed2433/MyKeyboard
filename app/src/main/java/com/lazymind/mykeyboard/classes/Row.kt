package com.lazymind.mykeyboard.classes

import android.graphics.Bitmap

class Row(val rowNo:Int, val items:ArrayList<Item>, val gapType: GapType) {

    val size = items.size
    var totalWeight:Int = 0

    init {
        for(item in items){
            totalWeight += item.weight
        }
    }

    fun get(y:Int):Item{
        return items[y]
    }

    fun update( y:Int,  key:String? = null, weight: Int? = null, pressKey: String? = null,
                keyType: KeyType? = null, bitmaps: MutableList<Bitmap>? = null ) {

        val item = get(y)

        key?.let { item.key = it }
        weight?.let {
            totalWeight -= item.weight
            item.weight = it
            totalWeight += item.weight
        }
        pressKey?.let { item.pressKey = it }
        keyType?.let { item.keyType = it }
        bitmaps?.let { item.bitmaps = it }
    }

    fun hasIcon(y:Int):Boolean{
        return get(y).hasIcon()
    }

    fun updateTextWidthHeight(y:Int, textWidth:Float, textHeight:Float){
        get(y).updateTextWidthHeight(textWidth, textHeight)
    }

    fun getBitmapAtIndex(y:Int, index:Int): Bitmap?{
        return get(y).getBitmapAtIndex(index)
    }

    fun updateRectAndIcon(y:Int, left: Float, top: Float, right: Float, bottom: Float) {
        get(y).updateRectAndIcon(left, top, right,bottom)
    }

}
