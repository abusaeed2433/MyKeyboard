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

    var capsMode:CapsType = CapsType.OFF
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
                        switchCapsMode()
                        layoutListener.onRefreshRequest()
                    }
                    return item
                }
            }
        }
        return null
    }

    fun updateCapsModeIfNext(){
        if(capsMode == CapsType.NEXT){
            capsMode = CapsType.OFF
            layoutListener.onRefreshRequest()
        }
    }

    private fun switchCapsMode(){
        capsMode = when(capsMode){
            CapsType.OFF -> CapsType.NEXT
            CapsType.NEXT -> CapsType.ON
            CapsType.ON -> CapsType.OFF
        }
    }

    fun shouldMakeUpper():Boolean{
        return capsMode == CapsType.ON || capsMode == CapsType.NEXT
    }

    fun getIconFor(row:Row, y:Int, type: LayoutType):Bitmap?{
        val item = row.get(y)
        if(item.isCaps(this.layoutType)){
            if(capsMode == CapsType.NEXT)
                return row.getBitmapAtIndex(y,1)
            if(capsMode == CapsType.ON)
                return row.getBitmapAtIndex(y,2)

            return row.getBitmapAtIndex(y,0)
        }

        if(item.isCharDig(this.layoutType) && type != LayoutType.MAIN){
            return row.getBitmapAtIndex(y,1)
        }

        if(item.isSymbolSwitch(this.layoutType)){
            if(this.layoutType == LayoutType.SYMBOL){
                return row.getBitmapAtIndex(y,1)
            }
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

    enum class CapsType{
        OFF, NEXT, ON
    }
}
