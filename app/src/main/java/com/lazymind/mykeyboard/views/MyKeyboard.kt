package com.lazymind.mykeyboard.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import com.lazymind.mykeyboard.R
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.classes.KeyType
import com.lazymind.mykeyboard.classes.LayoutType


class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    companion object{
        const val TOP_GAP = 10
        const val RX = 6f
        const val RY = 6f
        const val LEFT_RIGHT_GAP = 0.02f // 2%
    }

    private val textPaint: Paint = Paint()
    private val wholeBackPaint: Paint = Paint()
    private val itemBackPaint: Paint = Paint()

    private var listener: MyKeyboardListener? = null
    private var keyWidth = 0f
    private var keyHeight = 0f

    private val mainLayout: Layout
    private val secondLayout: Layout
    private var isMainLayoutShowing = true
    private var isReady = false

    constructor(context: Context?):this(context, null)

    init {
        init()

        val listener = object : Layout.LayoutListener{
            override fun onRefreshRequest() {
                invalidate()
            }
        }

        this.mainLayout = mainLayout(listener)
        this.secondLayout = secondLayout(listener)
    }

    private fun init() {
        textPaint.isAntiAlias = true
        textPaint.textSize = context.resources.getDimension(R.dimen.key_size)
        textPaint.color = Color.BLACK

        wholeBackPaint.color = resources.getColor(R.color.keyboard_back,null)

        itemBackPaint.color = resources.getColor(R.color.key_back,null)
        itemBackPaint.strokeWidth = 1f
        itemBackPaint.strokeCap = Paint.Cap.ROUND

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                keyWidth = (width.toFloat() - 2f*width*LEFT_RIGHT_GAP) / mainLayout.maxWeight
                keyHeight = keyWidth + 0.2f * keyWidth //(height * .25f) / mainLayout.noOfRow

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

        canvas.drawRect(0f, 0f, width.toFloat(),  height.toFloat(), wholeBackPaint)

        for(row in getLayout().items){
            for(item in row){
                canvas.drawRoundRect(item.baseRect, RX, RY, itemBackPaint)
//                canvas.drawRect(item.baseRect, itemBackPaint)
                if(item.hasIcon){
                    getLayout().getIconFor(item).let { canvas.drawBitmap(it!!,null, item.iconRect, wholeBackPaint) }
                }
                else {
                    canvas.drawText(
                        if(getLayout().isCapsModeOn) item.key.uppercase() else item.key,
                        item.baseRect.left + (item.baseRect.width() - item.textWidth) / 2,
                        item.baseRect.bottom - (item.baseRect.height() - item.textHeight) / 2,
                        textPaint
                    )
                }
            }
        }
    }

    private fun processItems(){
        val baseGap = 8f
        val leftRightPad = LEFT_RIGHT_GAP * width

        for(r in 0 until getLayout().items.size){
            val row = getLayout().items[r]

            val gap = ( ( getLayout().maxWeight - getLayout().rowWeight[r] ) * keyWidth ) / (row.size+2)
            var startX = gap

            for(item in row){
                val x = startX + leftRightPad //item.y * keyWidth + startX
                val y = item.x * keyHeight + (baseGap*r) + TOP_GAP

                val textWidth = textPaint.measureText(item.key)
                val bounds = Rect()
                textPaint.getTextBounds(item.key, 0, item.key.length, bounds)
                val textHeight = bounds.height()

                item.textWidth = textWidth
                item.textHeight = textHeight.toFloat()

                item.updateRectAndIcon(x, y, x + (keyWidth * item.weight), y+keyHeight)

                startX += ( gap + keyWidth*item.weight)
            }
        }
    }

    private fun getNavBarHeight():Int {
        val id = context.resources.getIdentifier("navigation_bar_height","dimen","android")

        if(id > 0){
            return context.resources.getDimensionPixelSize(id)
        }
        return 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalRows: Int = getLayout().noOfRow

        val viewHeight = (totalRows * keyHeight).toInt() + getNavBarHeight()/3 + TOP_GAP
        val viewWidth = getLayout().maxWeight * keyWidth.toInt()

        println("Item height: $viewHeight and width: $viewWidth")

        val width = resolveSize(viewWidth, widthMeasureSpec)
        val height = resolveSize(viewHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    private fun getLayout():Layout{
        return if(isMainLayoutShowing) mainLayout else secondLayout
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN){
            println("Processing key press")
            val item:Item? = getItemAt(event.x, event.y)
            if(item == null){
                println("Key is null")
                return true
            }
            println("key is not null")

            listener?.onKeyClicked(item, getLayout().isCapsModeOn)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getItemAt(x: Float, y: Float): Item? {
        val item = getLayout().getHolderId(x,y) ?: return null
        return item
    }


    // Layout initializer
    private fun mainLayout(listener: Layout.LayoutListener):Layout{
        val noOfRow = 5
        val items = Array(noOfRow){ArrayList<Item>()}

        items[0] = calcRows(0,6,"______") // _ means special, will be updated later
        items[1] = calcRows(1,10,"qwertyuiop", "1234567890")
        items[2] = calcRows(2,9,"asdfghjkl")
        items[3] = calcRows(3,9,"_zxcvbnm_")
        items[4] = calcRows(4,6,"_,__._")

        // updating special item
        for(sp in KeyType.entries){
            val x = sp.x
            val y = sp.y

            if(x == -1 || y == -1) continue

            val item = items[x][y]

            if(item.isSpace()){
                item.update(key="space", weight = sp.weight)
            }
            else if(item.isBackSpace()){
                item.update( weight = sp.weight, bitmaps = mutableListOf( readBitmap(R.drawable.backspace) ) )
            }
            else if(item.isNext()){
                item.update(
                    weight = sp.weight,
                    bitmaps = mutableListOf( readBitmap(R.drawable.right_arrow) )
                )
            }
            else if(item.isCaps()){
                item.update(
                    weight = sp.weight,
                    bitmaps = mutableListOf( readBitmap(R.drawable.arrow_up_normal), readBitmap(R.drawable.arrow_up_filled) )
                )
            }
            else{
                item.update(weight = sp.weight)
            }
        }

        return Layout( noOfRow, items, LayoutType.MAIN , layoutListener = listener)
    }

    private fun readBitmap(id:Int):Bitmap{
        return BitmapFactory.decodeResource(context.resources, id)
    }

    private fun calcRows(row:Int, size:Int, keys:String,pressKeys:String?=null):ArrayList<Item>{
        val list = ArrayList<Item>()

        for(i in 0 until size){
            list.add(
                Item( row,i, keys[i].toString(), pressKey = if(pressKeys == null) null else pressKeys[i].toString(), keyType = getKeyType(row,i) )
            )
        }
        return list
    }

    private fun getKeyType(x:Int, y:Int):KeyType{
        for(type in KeyType.entries){
            if(type.x == x && type.y == y) return type
        }
        return KeyType.NORMAL
    }

    private fun secondLayout(listener: Layout.LayoutListener):Layout{
        return mainLayout(listener)
    }

    fun setOnKeyboardActionListener(listener: MyKeyboardListener?) {
        this.listener = listener
    }

    interface MyKeyboardListener {
        fun onKeyClicked(item: Item, isCapsModeOn:Boolean)
    }

}
