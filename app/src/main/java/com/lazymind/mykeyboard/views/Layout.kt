package com.lazymind.mykeyboard.views

class Layout(
    val noOfRow:Int,
    val items: Array<ArrayList<Item>>,
    val type:Type
) {

    fun getItemAt(x:Int, y: Int):Item?{
        if(x < 0 || x >= noOfRow || y < 0 || y >= items[x].size) return null

        return items[x][y]
    }

    enum class Type{ MAIN, SECONDARY }
    class Item(val x:Int, val y:Int, val key:String, val pressKey:String?=null)
}
