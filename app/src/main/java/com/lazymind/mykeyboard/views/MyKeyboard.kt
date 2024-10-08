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
import com.lazymind.mykeyboard.classes.SpecialRow


class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    companion object{
        const val TOP_GAP = 10
        const val RX = 12f
        const val RY = 12f
        const val LEFT_RIGHT_GAP = 0.01f // 2%
    }

    private val textPaint: Paint = Paint()
    private val wholeBackPaint: Paint = Paint()
    private val itemBackPaint: Paint = Paint()
    private val commandBackPaint: Paint = Paint()
    private val clickedBackPaint: Paint = Paint()

    private var listener: MyKeyboardListener? = null
    private var keyWidth = 0f
    private var keyHeight = 0f
    private var specialRowHeight = 0f

    private val topRow:SpecialRow

    private val mainLayout: Layout
    private val digitSymbolLayout: Layout
    private val symbolLayout: Layout

    private val allLayouts:List<Layout>

    private var currentLayoutType:LayoutType = LayoutType.MAIN
    private var isSuggestionShowing = false
    private var isReady = false

    private var iconGenerator: IconGenerator
    private var clickedKey:Item? = null

    constructor(context: Context?):this(context, null)

    init {

        val listener = object : Layout.LayoutListener{
            override fun onRefreshRequest() {
                invalidate()
            }
        }

        this.iconGenerator = IconGenerator(context!!)
        this.topRow = getTopRow()
        this.mainLayout = mainLayout(listener)
        this.digitSymbolLayout = digitSymbol(listener)
        this.symbolLayout = symbolLayout(listener)

        this.allLayouts = listOf(mainLayout, digitSymbolLayout,symbolLayout)

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

        commandBackPaint.color = resources.getColor(R.color.command_key_back,null)
        commandBackPaint.strokeWidth = 1f
        commandBackPaint.strokeCap = Paint.Cap.ROUND

        clickedBackPaint.color = resources.getColor(R.color.clicked_key_back,null)
        clickedBackPaint.strokeWidth = 1f
        clickedBackPaint.strokeCap = Paint.Cap.ROUND

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                keyWidth = (width.toFloat() - 2f*width*LEFT_RIGHT_GAP) / mainLayout.maxWeight
                keyHeight = keyWidth + 0.2f * keyWidth //(height * .25f) / mainLayout.noOfRow
                specialRowHeight = keyWidth + 0.8f

                updateHeight(specialRowHeight, keyHeight)

                processSpecialRow()
                for(layout in allLayouts) {
                    processItems(layout)
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

        // First special row
        if(isSuggestionShowing){
            val specialRow = getLayout().topRow // row = topRow

            for(y in 0 until specialRow.size){
                val item = specialRow.get(y)

//                canvas.drawRoundRect(item.borderRect, RX,RY, specialRow.backPaint)

                if(y < specialRow.size-1) {
                    canvas.drawLine(
                        item.borderRect.right,
                        specialRow.lineTopPoint,
                        item.borderRect.right,
                        specialRow.lineBottomPoint,
                        specialRow.backPaint
                    )
                }

                canvas.drawText(
                    if(getLayout().shouldMakeUpper()) specialRow.suggestions[y].uppercase() else specialRow.suggestions[y],
                    item.borderRect.left + (item.borderRect.width() - item.textWidth) / 2,
                    item.borderRect.bottom - (item.borderRect.height() - item.textHeight) / 2,
                    specialRow.textPaint
                )
            }
        }

        // 0-all if suggestion is not showing else 1-all
        for(x in (if(isSuggestionShowing) 1 else 0) until getLayout().noOfRow){
            val row = getLayout().items[x]

            for(y in 0 until row.size){
                val item = row.get(y)

                canvas.drawRoundRect(
                    item.borderRect,
                    RX, RY,
                    if(item.keyType.isACommand()) commandBackPaint else {
                        if(item == clickedKey) clickedBackPaint else itemBackPaint
                    }
                )

                if(row.hasIcon(y)){
                    getLayout().getIconFor(row,y,currentLayoutType).let { canvas.drawBitmap(it!!,null, item.iconRect, wholeBackPaint) }
                }
                else {
                    canvas.drawText(
                        if(getLayout().shouldMakeUpper()) item.key.uppercase() else item.key,
                        item.borderRect.left + (item.borderRect.width() - item.textWidth) / 2,
                        item.borderRect.bottom - (item.borderRect.height() - item.textHeight) / 2,
                        textPaint
                    )
                }
            }
        }
    }

    private fun updateHeight(specialRowHeight: Float, keyHeight:Float){
        topRow.height = specialRowHeight
        for(layout in this.allLayouts){
            layout.topRow.height = specialRowHeight
            layout.items[0].height = specialRowHeight

            for(col in 1 until layout.noOfRow){
                layout.items[col].height = keyHeight
            }
        }
    }

    private fun processSpecialRow(){
        val leftRightPad = LEFT_RIGHT_GAP * width

        val row = topRow

        val gap = calcGap(getLayout(),row)
        var startX = gap

        if(row.gapType == GapType.NO_START_END_GAP) { startX = 0f }

        for(col in 0 until row.size){
            val item = row.get(col)

            val coorX = startX + leftRightPad //item.y * keyWidth + startX
            val coorY = TOP_GAP.toFloat()

            val textWidth = textPaint.measureText(item.key)
            val bounds = Rect()
            textPaint.getTextBounds(item.key, 0, item.key.length, bounds)
            val textHeight = bounds.height()

            row.updateTextWidthHeight(col, textWidth, textHeight.toFloat())
            row.updateRectAndIcon(col, coorX, coorY, coorX + (keyWidth * item.weight), coorY+specialRowHeight)

            if(row.gapType == GapType.NO_START_END_GAP){
                if(col == 0){
                    row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+specialRowHeight)
                    startX += gap
                }
                else if(col == row.size-1){
                    row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+specialRowHeight)
                }
            }

            startX += keyWidth*item.weight
            if(row.gapType == GapType.EVENLY) {
                startX += gap
            }
        }
    }

    private fun processItems(layout: Layout){
        val rowGap = context.resources.getDimension(R.dimen.row_gap)
        val leftRightPad = LEFT_RIGHT_GAP * width

        var startY = TOP_GAP.toFloat()
        for(r in 0 until layout.noOfRow){
            val row = layout.items[r]

            val gap = calcGap(layout,row)
            var startX = gap
            if(row.gapType == GapType.NO_START_END_GAP){ startX = 0f }

            for(col in 0 until row.size){
                val item = row.get(col)

                val coorX = startX + leftRightPad //item.y * keyWidth + startX
                val coorY = startY

                //val coorY = item.x * keyHeight + (baseGap*r) + TOP_GAP

                val textWidth = textPaint.measureText(item.key)
                val bounds = Rect()
                textPaint.getTextBounds(item.key, 0, item.key.length, bounds)
                val textHeight = bounds.height()- bounds.bottom

                row.updateTextWidthHeight(col, textWidth, textHeight.toFloat())
                row.updateRectAndIcon(col, coorX, coorY, coorX + (keyWidth * item.weight), coorY+row.height)

                if(row.gapType == GapType.NO_START_END_GAP){
                    if(col == 0){
                        row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+row.height)
                        startX += gap
                    }
                    else if(col == row.size-1){
                        row.updateRectAndIcon(col, coorX, coorY, coorX + (gap+ keyWidth * item.weight), coorY+row.height)
                    }
                }

                startX += keyWidth*item.weight
                if(row.gapType == GapType.EVENLY) {
                    startX += gap
                }
            }

            startY += row.height + rowGap
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

        val viewHeight = ((totalRows-1) * keyHeight + specialRowHeight).toInt() + getNavBarHeight()/3 + TOP_GAP
        val viewWidth = getLayout().maxWeight * keyWidth.toInt()

        println("Item height: $viewHeight and width: $viewWidth")

        val width = resolveSize(viewWidth, widthMeasureSpec)
        val height = resolveSize(viewHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    private fun getLayout():Layout{
        if(currentLayoutType == LayoutType.DIGIT_SYMBOL) return digitSymbolLayout

        if(currentLayoutType == LayoutType.SYMBOL) return symbolLayout

        return mainLayout
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN){
            val pair:Pair<Item,Boolean> = getItemAt(event.x, event.y) ?: return false
            val item = pair.first

            if(pair.second){ // special
                if(item.y == 0){
                    isSuggestionShowing = false
                    invalidate()
                    return true
                }
                listener?.onSpecialClicked(topRow.suggestions[item.y], getLayout().shouldMakeUpper())
                return true
            }

            if(item.isCharDig(currentLayoutType)){
                currentLayoutType = if(currentLayoutType == LayoutType.MAIN ) LayoutType.DIGIT_SYMBOL else LayoutType.MAIN
                invalidate()
                return true
            }

            if(item.isSymbolSwitch(currentLayoutType)){
                currentLayoutType = if(currentLayoutType == LayoutType.DIGIT_SYMBOL) LayoutType.SYMBOL else LayoutType.DIGIT_SYMBOL
                invalidate()
                return true
            }

            if(item.isBackSpace(currentLayoutType)){
                listener?.onBackSpaceClickDown()
                return true
            }

            clickedKey = item
            invalidate()

            listener?.onKeyClicked(currentLayoutType,item, getLayout().shouldMakeUpper())
            return true
        }

        if(event?.action == MotionEvent.ACTION_UP){
            listener?.onBackSpaceClickUp()

            postDelayed({
                clickedKey = null
                invalidate()
            },100)
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    private fun getItemAt(x: Float, y: Float): Pair<Item,Boolean>? {
        val spec = getLayout().getSpecialId(x,y)

        if(spec != null){
            return Pair(spec,true)
        }

        val item = getLayout().getHolderId(x,y) ?: return null
        return Pair(item,false)
    }

    // Layout initializer
    private fun getTopRow():SpecialRow{
        val itemZero = Item(0,0,"x", weight = KeyType.CANCEL_PREVIEW.weight, keyType = KeyType.CANCEL_PREVIEW)
        val itemOne = Item(0,1,"", weight = KeyType.SUGGEST_ONE.weight, keyType = KeyType.SUGGEST_ONE)
        val itemTwo = Item(0,2,"", weight = KeyType.SUGGEST_TWO.weight, keyType = KeyType.SUGGEST_TWO)
        val itemThree = Item(0,3,"", weight = KeyType.SUGGEST_THREE.weight, keyType = KeyType.SUGGEST_THREE)

        val items = ArrayList<Item>()
        items.add(itemZero)
        items.add(itemOne)
        items.add(itemTwo)
        items.add(itemThree)

        val textSizeF = context.resources.getDimension(R.dimen.key_size)
        return SpecialRow.getInstance(textSizeF/25,0,items, GapType.EVENLY) // 25sp see dimen.xml
    }

    fun updateCapsModeIfNeeded(){
        getLayout().updateCapsModeIfNext()
    }

    fun startSingleCaps(){
        getLayout().startSingleCapsIfInMain()
    }

    fun restartKeyboard(){
        currentLayoutType = LayoutType.MAIN
        isSuggestionShowing = false
        clickedKey = null
        startSingleCaps()

        invalidate()
    }

    private fun mainLayout(listener: Layout.LayoutListener):Layout{
        val noOfRow = 5
        val items = ArrayList<Row>()

        items.add(calcBasicRows(LayoutType.MAIN,0,6,"______", gapType = GapType.START_END)) // _ means special, will be updated later
        items.add(calcBasicRows(LayoutType.MAIN,1,10,"qwertyuiop", "1234567890"))
        items.add(calcBasicRows(LayoutType.MAIN,2,9,"asdfghjkl", gapType = GapType.START_END))
        items.add(calcBasicRows(LayoutType.MAIN,3,9,"_zxcvbnm_", gapType = GapType.NO_START_END_GAP))
        items.add(calcBasicRows(LayoutType.MAIN,4,5,"_,_._", gapType = GapType.NO_START_END_GAP))

        // updating special item
        for(kt in KeyType.entries){
            val x = kt.x
            val y = kt.y

            if(!kt.doesHave(LayoutType.MAIN)) continue
//            if(kt.layoutTypes != LayoutType.MAIN && kt.layoutTypes != LayoutType.COMMON) continue

            if(x == -1 || y == -1) continue

            val row = items[x]
            val item = row.get(y)
            updateItem(LayoutType.MAIN,row, item, y,kt)
        }

        return Layout(noOfRow, this.topRow, items, LayoutType.MAIN , layoutListener = listener)
    }

    private fun digitSymbol(listener: Layout.LayoutListener):Layout{
        val noOfRow = 5
        val items = ArrayList<Row>()

        items.add(this.mainLayout.items[0]) // reusing first special row
        items.add(calcBasicRows(LayoutType.DIGIT_SYMBOL,1,10,"1234567890"))
        items.add(calcBasicRows(LayoutType.DIGIT_SYMBOL,2,10,"@#\$_&-+()/", gapType = GapType.START_END))
        items.add(calcBasicRows(LayoutType.DIGIT_SYMBOL,3,9,"_*\"':;!?_", gapType = GapType.NO_START_END_GAP))
        items.add(calcBasicRows(LayoutType.DIGIT_SYMBOL,4,5,"_,_._", gapType = GapType.NO_START_END_GAP))

        // updating special item
        for(kt in KeyType.entries){
            val x = kt.x
            val y = kt.y

            if(!kt.doesHave(LayoutType.DIGIT_SYMBOL)) continue
            //if(kt.layoutTypes != LayoutType.DIGIT_SYMBOL && kt.layoutTypes != LayoutType.COMMON) continue

            if(x == -1 || y == -1) continue

            val row = items[x]
            val item = row.get(y)
            updateItem(LayoutType.DIGIT_SYMBOL,row, item, y, kt)
        }

        return Layout( noOfRow, this.topRow, items, LayoutType.DIGIT_SYMBOL , layoutListener = listener)
    }

    private fun symbolLayout(listener: Layout.LayoutListener):Layout {
        val noOfRow = 5
        val items = ArrayList<Row>()

        items.add(this.mainLayout.items[0]) // reusing first special row
        items.add(calcBasicRows(LayoutType.SYMBOL,1,10,"~`|•√π÷×§∆"))
        items.add(calcBasicRows(LayoutType.SYMBOL,2,10,"€¥\$¢^°={}\\", gapType = GapType.START_END))
        items.add(calcBasicRows(LayoutType.SYMBOL,3,9,"_%©®™✓[]_", gapType = GapType.NO_START_END_GAP))
        items.add(calcBasicRows(LayoutType.SYMBOL,4,5,"_,_._", gapType = GapType.NO_START_END_GAP))

        // updating special item
        for(kt in KeyType.entries){
            val x = kt.x
            val y = kt.y

            if(!kt.doesHave(LayoutType.SYMBOL)) continue

            if(x == -1 || y == -1) continue

            val row = items[x]
            val item = row.get(y)
            updateItem(LayoutType.SYMBOL,row, item, y, kt)
        }

        return Layout( noOfRow, this.topRow, items, LayoutType.SYMBOL , layoutListener = listener)
    }

    private fun updateItem(layoutType: LayoutType, row:Row, item:Item, y:Int, kt:KeyType){
        if(item.isSpace(layoutType)){
            row.update(y, key="", weight = kt.weight)
        }
        else if(item.isBackSpace(layoutType)){
            row.update(
                y,
                weight = kt.weight,
                bitmaps = iconGenerator.getIconFor(KeyType.BACKSPACE)
                //bitmaps = mutableListOf( readBitmap(R.drawable.backspace) )
            )
        }
        else if(item.isNext(layoutType)){
            row.update(y,
                weight = kt.weight,
                bitmaps = iconGenerator.getIconFor(KeyType.NEXT)
                //bitmaps = mutableListOf( readBitmap(R.drawable.right_arrow) )
            )
        }
        else if(item.isCaps(layoutType)){
            row.update(y,
                weight = kt.weight,
                bitmaps = iconGenerator.getIconFor(KeyType.CAPS)
                //bitmaps = mutableListOf( readBitmap(R.drawable.arrow_up_normal), readBitmap(R.drawable.arrow_up_filled) )
            )
        }
        else if(item.isCharDig(layoutType)){
            row.update(y,
                weight = kt.weight,
                bitmaps = iconGenerator.getIconFor(KeyType.CHAR_DIG)
                //bitmaps = mutableListOf( readBitmap(R.drawable.ic_char), readBitmap(R.drawable.ic_digit) )
            )
        }
        else if(item.isSymbolSwitch(layoutType)){
            row.update(y,
                weight = kt.weight,
                bitmaps = iconGenerator.getIconFor(KeyType.SYMBOL_SWITCH)
//                bitmaps = mutableListOf( readBitmap(R.drawable.ic_more) )
            )
        }
        else{
            row.update(y,weight = kt.weight)
        }
    }

    private fun readBitmap(id:Int):Bitmap{
        return BitmapFactory.decodeResource(context.resources, id)
    }

    private fun calcBasicRows(layoutType:LayoutType, row:Int, size:Int,
                              keys:String, pressKeys:String? = null, gapType: GapType = GapType.EVENLY):Row {
        val list = ArrayList<Item>()

        for(col in 0 until size){
            list.add(
                Item(
                    row, col, keys[col].toString(),
                    pressKey = if(pressKeys == null) null else pressKeys[col].toString(),
                    keyType = getKeyType(layoutType,row,col)
                )
            )
        }

        return Row(row, list, gapType)
    }

    fun showSuggestion(words:Array<String>){
        isSuggestionShowing = true
        topRow.update(words)
        invalidate()
    }

    private fun getKeyType(layoutType: LayoutType, x:Int, y:Int):KeyType{
        for(type in KeyType.entries){
            if(type.doesHave(layoutType) && (type.x == x && type.y == y)) return type
        }
        return KeyType.NORMAL
    }

    fun setOnKeyboardActionListener(listener: MyKeyboardListener?) {
        this.listener = listener
    }

    interface MyKeyboardListener {
        fun onKeyClicked(layoutType: LayoutType,item: Item, isCapsModeOn:Boolean)
        fun onSpecialClicked(str: String, isCapsModeOn:Boolean)
        fun onBackSpaceClickDown()
        fun onBackSpaceClickUp()
    }

}
