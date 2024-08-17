package com.lazymind.mykeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.views.MyKeyboard


class MyKeyboardService : InputMethodService(), MyKeyboard.MyKeyboardListener{

    private lateinit var myKeyboard: MyKeyboard

    override fun onCreateInputView(): View {
        myKeyboard =  MyKeyboard(this)
        myKeyboard.setOnKeyboardActionListener(this)

        window.window?.navigationBarColor = resources.getColor(R.color.keyboard_back,null)

        return myKeyboard
    }

    override fun onKeyClicked(item: Item, isCapsModeOn:Boolean) {
        val inputConnection = currentInputConnection

        if(item.isCaps()) return

        if(item.isBackSpace()){
            inputConnection.deleteSurroundingText(1,0)
        }
        else if(item.isSpace()){
            inputConnection.commitText(" ",1)
        }
        else if(item.isNext()){
            handleEditorAction(inputConnection, currentInputEditorInfo)
        }
        else {
            inputConnection?.commitText(
                if(isCapsModeOn) item.key.uppercase() else item.key.lowercase(),
                1
            )
        }
    }

    private fun handleEditorAction(inputConnection: InputConnection, editorInfo: EditorInfo) {
        // Determine the action from the EditorInfo
        val imeAction = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION

        when (imeAction) {
            EditorInfo.IME_ACTION_SEARCH ->             // Perform search action
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH)

            EditorInfo.IME_ACTION_GO ->             // Perform go action
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_GO)

            EditorInfo.IME_ACTION_SEND ->             // Perform send action
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)

            EditorInfo.IME_ACTION_DONE ->             // Perform done action
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)

            EditorInfo.IME_ACTION_NEXT ->             // Move to the next field
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_NEXT)

            else ->             // If no specific action, insert a newline
                inputConnection.commitText("\n", 1)
        }
    }

}
