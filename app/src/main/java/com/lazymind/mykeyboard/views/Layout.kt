package com.lazymind.mykeyboard.views

import android.graphics.Bitmap
import android.graphics.RectF
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.classes.LayoutType
import kotlin.math.max

class Layout(
    val noOfRow:Int,
    val items: Array<ArrayList<Item>>,
    val layoutType: LayoutType,
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
}
