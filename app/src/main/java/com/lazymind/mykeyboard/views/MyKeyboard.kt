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
import com.lazymind.mykeyboard.classes.GapType
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.classes.KeyType
import com.lazymind.mykeyboard.classes.LayoutType
import com.lazymind.mykeyboard.classes.Row


class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    companion object{
        const val TOP_GAP = 10
        const val RX = 6f
        const val RY = 6f
        const val LEFT_RIGHT_GAP = 0.01f // 2%
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
    private var layoutType:LayoutType = LayoutType.MAIN

    constructor(context: Context?):this(context, null)

    init {

        val listener = object : Layout.LayoutListener{
            override fun onRefreshRequest() {
                invalidate()
            }
        }

        this.mainLayout = mainLayout(listener)
        this.secondLayout = secondLayout(listener)

        init()
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

                for(layout in listOf(mainLayout, secondLayout)) {
                    processItems(mainLayout)
                    processItems(secondLayout)
                }

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
            for(y in 0 until row.size){
                val item = row.get(y)

                canvas.drawRoundRect(item.borderRect, RX, RY, itemBackPaint)
                if(row.hasIcon(y)){
                    getLayout().getIconFor(row,y,layoutType).let { canvas.drawBitmap(it!!,null, item.iconRect, wholeBackPaint) }
                }
                else {
                    canvas.drawText(
                        if(getLayout().isCapsModeOn) item.key.uppercase() else item.key,
                        item.borderRect.left + (item.borderRect.width() - item.textWidth) / 2,
                        item.borderRect.bottom - (item.borderRect.height() - item.textHeight) / 2,
                        textPaint
                    )
                }
            }
        }
    }

    private fun processItems(layout: Layout){
        val baseGap = 8f
        val leftRightPad = LEFT_RIGHT_GAP * width

        for(r in 0 until layout.noOfRow){
            val row = layout.items[r]

            val gap = calcGap(layout,row)
            var startX = gap
            if(row.gapType == GapType.NO_START_END_GAP){ startX = 0f }

            for(col in 0 until row.size){
                val item = row.get(col)

                val coorX = startX + leftRightPad //item.y * keyWidth + startX
                val coorY = item.x * keyHeight + (baseGap*r) + TOP_GAP

                val textWidth = textPaint.measureText(item.key)
                val bounds = Rect()
                textPaint.getTextBounds(item.key, 0, item.key.length, bounds)
                val textHeight = bounds.height()

                row.updateTextWidthHeight(col, textWidth, textHeight.toFloat())
                row.updateRectAndIcon(col, coorX, coorY, coorX + (keyWidth * item.weight), coorY+keyHeight)

                if(row.gapType == GapType.NO_START_END_GAP){
                    if(col == 0){
                        row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+keyHeight)
                        startX += gap
                    }
                    else if(col == row.size-1){
                        row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+keyHeight)
                    }
                }

                startX += keyWidth*item.weight
                if(row.gapType == GapType.EVENLY) {
                    startX += gap
                }
            }
        }
    }

    private fun calcGap(layout: Layout,row:Row):Float {

        val mainGap = ( layout.maxWeight - row.totalWeight ) * keyWidth

        if(row.gapType == GapType.EVENLY){
            return mainGap / (row.size+2)
        }

        return mainGap / 2f
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
            val item:Item = getItemAt(event.x, event.y) ?: return true

            if(item.isCharDig()){
                layoutType = if(isMainLayoutShowing) LayoutType.SECONDARY else LayoutType.MAIN
                isMainLayoutShowing = !isMainLayoutShowing
                invalidate()
                return true
            }

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
        val items = ArrayList<Row>()

        items.add(calcBasicRows(0,6,"______", gapType = GapType.START_END)) // _ means special, will be updated later
        items.add(calcBasicRows(1,10,"qwertyuiop", "1234567890"))
        items.add(calcBasicRows(2,9,"asdfghjkl", gapType = GapType.START_END))
        items.add(calcBasicRows(3,9,"_zxcvbnm_", gapType = GapType.NO_START_END_GAP))
        items.add(calcBasicRows(4,5,"_,_._", gapType = GapType.NO_START_END_GAP))

        // updating special item
        for(kt in KeyType.entries){
            val x = kt.x
            val y = kt.y

            if(kt.layoutType != LayoutType.MAIN && kt.layoutType != LayoutType.COMMON) continue

            if(x == -1 || y == -1) continue

            val row = items[x]
            val item = row.get(y)
            updateItem(row, item, y,kt)
        }

        return Layout( noOfRow, items, LayoutType.MAIN , layoutListener = listener)
    }

    private fun secondLayout(listener: Layout.LayoutListener):Layout{
        val noOfRow = 5
        val items = ArrayList<Row>()

        items.add(calcBasicRows(0,6,"______", gapType = GapType.START_END)) // _ means special, will be updated later
        items.add(calcBasicRows(1,10,"1234567890"))
        items.add(calcBasicRows(2,10,"@#\$_&-+()/", gapType = GapType.START_END))
        items.add(calcBasicRows(3,9,"=*\"':;!?_", gapType = GapType.NO_START_END_GAP))
        items.add(calcBasicRows(4,5,"_,_._", gapType = GapType.NO_START_END_GAP))

        // updating special item
        for(kt in KeyType.entries){
            val x = kt.x
            val y = kt.y

            if(kt.layoutType != LayoutType.SECONDARY && kt.layoutType != LayoutType.COMMON) continue

            if(x == -1 || y == -1) continue

            val row = items[x]
            val item = row.get(y)
            updateItem(row, item, y, kt)
        }

        return Layout( noOfRow, items, LayoutType.MAIN , layoutListener = listener)
    }

    private fun updateItem(row:Row, item:Item, y:Int, kt:KeyType){
        if(item.isSpace()){
            row.update(y, key="", weight = kt.weight)
        }
        else if(item.isBackSpace()){
            row.update(y, weight = kt.weight, bitmaps = mutableListOf( readBitmap(R.drawable.backspace) ) )
        }
        else if(item.isNext()){
            row.update(y,
                weight = kt.weight,
                bitmaps = mutableListOf( readBitmap(R.drawable.right_arrow) )
            )
        }
        else if(item.isCaps()){
            row.update(y,
                weight = kt.weight,
                bitmaps = mutableListOf( readBitmap(R.drawable.arrow_up_normal), readBitmap(R.drawable.arrow_up_filled) )
            )
        }
        else if(item.isCharDig()){
            row.update(y,
                weight = kt.weight,
                bitmaps = mutableListOf( readBitmap(R.drawable.ic_char), readBitmap(R.drawable.ic_digit) )
            )
        }
        else{
            row.update(y,weight = kt.weight)
        }
    }

    private fun readBitmap(id:Int):Bitmap{
        return BitmapFactory.decodeResource(context.resources, id)
    }

    private fun calcBasicRows(row:Int, size:Int, keys:String, pressKeys:String? = null, gapType: GapType = GapType.EVENLY):Row{
        val list = ArrayList<Item>()

        for(col in 0 until size){
            list.add(
                Item( row, col, keys[col].toString(), pressKey = if(pressKeys == null) null else pressKeys[col].toString(), keyType = getKeyType(row,col) )
            )
        }

        return Row(row, list, gapType)
    }

    private fun getKeyType(x:Int, y:Int):KeyType{
        for(type in KeyType.entries){
            if(type.x == x && type.y == y) return type
        }
        return KeyType.NORMAL
    }

    fun setOnKeyboardActionListener(listener: MyKeyboardListener?) {
        this.listener = listener
    }

    interface MyKeyboardListener {
        fun onKeyClicked(item: Item, isCapsModeOn:Boolean)
    }

}
