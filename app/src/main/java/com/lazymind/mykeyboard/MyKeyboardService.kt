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

        window.window?.navigationBarColor = resources.getColor(R.color.keyboard_back,null)

        return myKeyboard
    }

    override fun onKeyPress(item: Layout.Item, isCapsModeOn:Boolean) {
        val inputConnection = currentInputConnection

        if(item.isCaps()) return

        if(item.isBackSpace()){
            inputConnection.deleteSurroundingText(1,0)
        }
        else if(item.isSpace()){
            inputConnection.commitText(" ",1)
        }
        else {
            inputConnection?.commitText(
                if(isCapsModeOn) item.key.uppercase() else item.key.lowercase(),
                1
            )
        }
    }

}
