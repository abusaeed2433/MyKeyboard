package com.lazymind.mykeyboard.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class MyKeyboard(context: Context?, attrs: AttributeSet?):View(context, attrs) {

    private var paint: Paint? = null
    private var listener: OnKeyboardActionListener? = null
    private var keyWidth = 0f
    private var keyHeight = 0f

    private val keys = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z"
    )

    constructor(context: Context?):this(context, null)

    init {
        init()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = Color.BLACK
        paint!!.textSize = 20f
        keyWidth = 100f // Example key width
        keyHeight = 100f // Example key height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in keys.indices) {
            val x = (i % 10) * keyWidth
            val y = (i / 10) * keyHeight

            // Draw key background
            paint!!.color = Color.LTGRAY
            canvas.drawRect(x, y, x + keyWidth, y + keyHeight, paint!!)

            // Draw key label
            paint!!.color = Color.BLACK
            val textWidth = paint!!.measureText(keys[i])
            canvas.drawText(
                keys[i], x + (keyWidth - textWidth) / 2, y + keyHeight / 2 + 20,
                paint!!
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalRows: Int = (keys.size + 9) / 10
        val desiredHeight = (totalRows * keyHeight).toInt()
        val desiredWidth = (10 * keyWidth).toInt() // Assuming 10 columns

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val index = getKeyIndex(event.x, event.y)
            if (index != -1 && listener != null) {
                listener!!.onKeyPress(keys[index])
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun getKeyIndex(x: Float, y: Float): Int {
        val column = (x / keyWidth).toInt()
        val row = (y / keyHeight).toInt()
        val index = row * 10 + column

        if (index >= 0 && index < keys.size) {
            return index
        }
        return -1
    }

    // Set listener for key presses
    fun setOnKeyboardActionListener(listener: OnKeyboardActionListener?) {
        this.listener = listener
    }

    // Interface to communicate key presses
    interface OnKeyboardActionListener {
        fun onKeyPress(key: String?)
    }

}