package com.lazymind.mykeyboard.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import com.lazymind.mykeyboard.R

class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    private val textPaint: Paint = Paint()
    private val wholeBackPaint: Paint = Paint()
    private val itemBackPaint: Paint = Paint()

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

        val listener = object : Layout.LayoutListener{
            override fun onRefreshRequest() {
                invalidate()
            }
        }

        this.mainLayout = mainLayout(listener)
        this.secondLayout = secondLayout(listener)
    }

    private fun init() {

        for(paint in listOf(textPaint, itemBackPaint, wholeBackPaint)) {
            paint.isAntiAlias = true
            paint.textSize = 26f
            paint.color = Color.BLACK
        }
        wholeBackPaint.color = Color.CYAN
        itemBackPaint.color = Color.argb(200,240,240,240)
        itemBackPaint.strokeWidth = 1f
        itemBackPaint.strokeCap = Paint.Cap.ROUND

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

        canvas.drawRect(0f, 0f, width.toFloat(),  height.toFloat(), wholeBackPaint)

        for(row in getLayout().items){
            for(item in row){
                if(item.hasIcon){
                    getLayout().getIconFor(item).let { canvas.drawBitmap(it!!,null, item.holderRect,wholeBackPaint) }
                }
                else {
                    canvas.drawText(
                        if(getLayout().isCapsModeOn) item.key.uppercase() else item.key,
                        item.baseRect.left + (item.baseRect.width() - item.textWidth) / 2,
                        item.baseRect.top + keyHeight / 2 + 20, textPaint
                    )
                }

                canvas.drawRect(item.baseRect, itemBackPaint)
            }
        }
    }

    private fun processItems(){
        val baseGap = 8f

        for(r in 0 until getLayout().items.size){
            val row = getLayout().items[r]

            val gap = ( ( getLayout().maxWeight - getLayout().rowWeight[r] ) * keyWidth ) / (row.size+2)
            var startX = gap

            for(item in row){
                val x = startX //item.y * keyWidth + startX
                val y = item.x * keyHeight + (baseGap*r)

                val textWidth = textPaint.measureText(item.key)
                item.textWidth = textWidth

                item.updateRectAndIcon(x, y, x + (keyWidth * item.weight), y+keyHeight)

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

    private fun mainLayout(listener: Layout.LayoutListener):Layout{
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

            if(x == -1 || y == -1) continue

            val item = items[x][y]

            if(item.isSpace()){
                items[x][y] = Layout.Item(x,y, "space", sp.weight, keyType = item.keyType)
            }
            else if(item.isBackSpace()){
                val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources,R.drawable.backspace)
                items[x][y] = Layout.Item(x,y,item.key,sp.weight, keyType = item.keyType, bitmaps = mutableListOf(bitmap))
            }
            else if(item.isCaps()){
                val bitmapOne: Bitmap = BitmapFactory.decodeResource(context.resources,R.drawable.arrow_up_normal)
                val bitmapTwo: Bitmap = BitmapFactory.decodeResource(context.resources,R.drawable.arrow_up_filled)

                items[x][y] = Layout.Item(x,y,item.key,sp.weight, keyType = item.keyType, bitmaps = mutableListOf(bitmapOne,bitmapTwo))
            }
            else{
                items[x][y] = Layout.Item(x,y, item.key, sp.weight, keyType = item.keyType)
            }
        }

        return Layout( noOfRow, items, Layout.LayoutType.MAIN , layoutListener = listener)
    }

    private fun calcRows(row:Int, size:Int, keys:String,pressKeys:String?=null):ArrayList<Layout.Item>{
        val list = ArrayList<Layout.Item>()

        for(i in 0 until size){
            list.add(
                Layout.Item( row,i, keys[i].toString(), pressKey = if(pressKeys == null) null else pressKeys[i].toString(), keyType = getKeyType(row,i) )
            )
        }
        return list
    }

    private fun getKeyType(x:Int, y:Int):Layout.KeyType{
        for(type in Layout.KeyType.entries){
            if(type.x == x && type.y == y) return type
        }
        return Layout.KeyType.NORMAL
    }

    private fun secondLayout(listener: Layout.LayoutListener):Layout{
        return mainLayout(listener)
    }

    fun setOnKeyboardActionListener(listener: OnKeyboardActionListener?) {
        this.listener = listener
    }

    interface OnKeyboardActionListener {
        fun onKeyPress(item: Layout.Item)
    }

}