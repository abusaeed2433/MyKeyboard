package com.lazymind.mykeyboard.views

import kotlin.math.max

class Layout(
    val noOfRow:Int,
    val items: Array<ArrayList<Item>>,
    val layoutType:LayoutType
) {

    val maxWeight:Int
    val rowWidth:IntArray = IntArray(noOfRow){0}

    init {
        var localMax = 0
        for(i in 0 until noOfRow){
            rowWidth[i] = 0
            for(j in 0 until items[i].size){
                rowWidth[i] += items[i][j].weight
            }

            localMax = max(localMax, rowWidth[i])
        }

        maxWeight = localMax
    }

    fun getItemAt(x:Int, y: Int):Item?{
        if(x < 0 || x >= noOfRow || y < 0 || y >= items[x].size) return null

        return items[x][y]
    }

    enum class LayoutType{ MAIN, SECONDARY }
    enum class KeyType(val x:Int, val y:Int, val weight:Int=1){
        OPTIONS(0,0), STICKER(0,1), GIF(0,2), CLIPBOARD(0,3), THEME(0,4), MIC(0,5),
        CAPS(3,0), BACKSPACE(3,8),
        CHAR_DIG(4,0), LANGUAGE(4,2), SPACE(4,3,4), NEXT(4,5)
    }
    class Item(val x:Int, val y:Int, val key:String, val weight: Int = 1, val pressKey:String?=null)
}
