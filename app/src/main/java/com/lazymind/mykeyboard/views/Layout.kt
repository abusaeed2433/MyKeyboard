package com.lazymind.mykeyboard.views

import android.graphics.Bitmap
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.classes.LayoutType
import com.lazymind.mykeyboard.classes.Row
import com.lazymind.mykeyboard.classes.SpecialRow
import kotlin.math.max

class Layout(
    val noOfRow:Int,
    val topRow: SpecialRow,
    val items: ArrayList<Row>,
    val layoutType: LayoutType,
    val layoutListener:LayoutListener
) {

    var isCapsModeOn = false
    val maxWeight:Int

    init {
        var localMax = 0
        for(row in items){
            localMax = max(localMax, row.totalWeight)
        }

        maxWeight = localMax
    }

    fun getSpecialId(x:Float, y:Float):Item?{
        for(item in topRow.items){
            if(item.baseRect.contains(x,y)) {
                if(item.isCaps(this.layoutType)){
                    isCapsModeOn = !isCapsModeOn
                    layoutListener.onRefreshRequest()
                }
                return item
            }
        }
        return null
    }

    fun getHolderId(x:Float, y:Float):Item?{
        for(row in  items){
            for(item in row.items){
                if(item.baseRect.contains(x,y)) {
                    if(item.isCaps(this.layoutType)){
                        isCapsModeOn = !isCapsModeOn
                        layoutListener.onRefreshRequest()
                    }
                    return item
                }
            }
        }
        return null
    }

    fun getIconFor(row:Row, y:Int, type: LayoutType):Bitmap?{
        val item = row.get(y)
        if(item.isCaps(this.layoutType) && isCapsModeOn){
            return row.getBitmapAtIndex(y,1)
        }
        if(item.isCharDig(this.layoutType) && type != LayoutType.MAIN){
            return row.getBitmapAtIndex(y,1)
        }
        return row.getBitmapAtIndex(y,0)
    }

    fun getItemAt(x:Int, y: Int):Item?{
        if(x < 0 || x >= noOfRow || y < 0 || y >= items[x].size) return null

        return items[x].get(y)
    }

    interface LayoutListener{
        fun onRefreshRequest()
    }
}
