package com.lazymind.mykeyboard.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    private var paint: Paint? = null
    private var listener: OnKeyboardActionListener? = null
    private var keyWidth = 0f
    private var keyHeight = 0f

    private val mainLayout: Layout
    private val secondLayout: Layout
    private var isMainLayoutShowing = true
    private var isReady = false

    constructor(context: Context?):this(context, null)

    init {
        init()

        this.mainLayout = mainLayout()
        this.secondLayout = secondLayout()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = Color.BLACK
        paint!!.textSize = 26f

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                keyWidth = (width.toFloat() / mainLayout.maxWeight)
                keyHeight = keyWidth //(height * .25f) / mainLayout.noOfRow

                processItems()
                requestLayout()
                isReady = true
                invalidate()
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(!isReady) return

        paint!!.color = Color.CYAN
        canvas.drawRect(0f, 0f, width.toFloat(),  height.toFloat(), paint!!)
        paint!!.color = Color.BLACK

        for(row in getLayout().items){
            for(item in row){
                canvas.drawText(
                    item.key,
                    item.rect.left + (item.rect.width() - item.textWidth)/2,
                    item.rect.top + keyHeight/2 + 20, paint!!
                )
            }
        }
    }

    private fun processItems(){
        for(r in 0 until getLayout().items.size){
            val row = getLayout().items[r]

            val gap = ( ( getLayout().maxWeight - getLayout().rowWeight[r] ) * keyWidth ) / (row.size+2)
            var startX = gap

            for(item in row){
                val x = startX //item.y * keyWidth + startX
                val y = item.x * keyHeight

                val textWidth = paint!!.measureText(item.key)
                item.textWidth = textWidth

                item.updateRect(x,y, x + (keyWidth * item.weight), y+keyHeight)

                startX += ( gap + keyWidth*item.weight)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalRows: Int = getLayout().noOfRow

        val itemHeight = (totalRows * keyHeight).toInt()
        val itemWidth = getLayout().maxWeight * keyWidth.toInt()

        println("Item height: $itemHeight and width: $itemWidth")

        val width = resolveSize(itemWidth, widthMeasureSpec)
        val height = resolveSize(itemHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    private fun getLayout():Layout{
        return if(isMainLayoutShowing) mainLayout else secondLayout
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN){
            println("Processing key press")
            val item:Layout.Item? = getItemAt(event.x, event.y)
            if(item == null){
                println("Key is null")
                return true
            }
            println("key is not null")

            listener?.onKeyPress(item)
        }
        return super.dispatchTouchEvent(event)
    }

//    @SuppressLint("ClickableViewAccessibility")
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_UP) {
//
//            println("Processing key press")
//            val key:String? = getKeyAt(event.x, event.y)
//            if(key == null){
//                println("Key is null")
//                return true
//            }
//            println("key is not null")
//
//            listener?.onKeyPress(key)
//
//            return true
//        }
//        return super.onTouchEvent(event)
//    }

    private fun getItemAt(x: Float, y: Float): Layout.Item? {
        val item = getLayout().getHolderId(x,y) ?: return null
        return item
    }

    private fun mainLayout():Layout{
        val noOfRow = 5
        val items = Array(noOfRow){ArrayList<Layout.Item>()}

        items[0] = calcRows(0,6,"______") // _ means special, will be updated later
        items[1] = calcRows(1,10,"qwertyuiop", "1234567890")
        items[2] = calcRows(2,9,"asdfghjkl")
        items[3] = calcRows(3,9,"_zxcvbnm_")
        items[4] = calcRows(4,6,"_,__._")

        // updating special item
        for(sp in Layout.KeyType.entries){
            val x = sp.x
            val y = sp.y

            if(items[x][y].isSpace()){
                items[x][y] = Layout.Item(x,y, "space", sp.weight)
            }
            else{
                items[x][y] = Layout.Item(x,y, items[x][y].key, sp.weight)
            }
        }

        return Layout( noOfRow, items, Layout.LayoutType.MAIN )
    }

    private fun calcRows(row:Int, size:Int, keys:String,pressKeys:String?=null):ArrayList<Layout.Item>{
        val list = ArrayList<Layout.Item>()

        for(i in 0 until size){
            list.add(
                Layout.Item( row,i, keys[i].toString(), pressKey = if(pressKeys == null) null else pressKeys[i].toString() )
            )
        }
        return list
    }

    private fun secondLayout():Layout{
        return mainLayout()
    }

    fun setOnKeyboardActionListener(listener: OnKeyboardActionListener?) {
        this.listener = listener
    }

    interface OnKeyboardActionListener {
        fun onKeyPress(item: Layout.Item)
    }

}