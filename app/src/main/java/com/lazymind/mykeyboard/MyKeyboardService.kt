package com.lazymind.mykeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import com.lazymind.mykeyboard.views.Layout
import com.lazymind.mykeyboard.views.MyKeyboard


class MyKeyboardService : InputMethodService(), MyKeyboard.OnKeyboardActionListener{

    private lateinit var myKeyboard: MyKeyboard

    override fun onCreateInputView(): View {
        myKeyboard =  MyKeyboard(this)
        myKeyboard.setOnKeyboardActionListener(this)

        return myKeyboard
    }

    override fun onKeyPress(item: Layout.Item) {
        val inputConnection = currentInputConnection

        if(item.isBackSpace()){
            inputConnection.deleteSurroundingText(1,0)
            return
        }

        inputConnection?.commitText(item.key, 1)
    }

}