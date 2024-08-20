package com.lazymind.mykeyboard

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.provider.UserDictionary
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.lazymind.mykeyboard.classes.Item
import com.lazymind.mykeyboard.classes.LayoutType
import com.lazymind.mykeyboard.views.MyKeyboard
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random


class MyKeyboardService : InputMethodService(){

    companion object{
        const val LETTER_DELETION_INTERVAL = 150L
    }

    private lateinit var myKeyboard: MyKeyboard

    override fun onCreateInputView(): View {
        setupMyKeyboard()

        window.window?.navigationBarColor = resources.getColor(R.color.keyboard_back,null)

        readWords()
        return myKeyboard
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)

        myKeyboard.restartKeyboard()
    }


    private fun setupMyKeyboard(){
        myKeyboard =  MyKeyboard(this)
        myKeyboard.setOnKeyboardActionListener(object : MyKeyboard.MyKeyboardListener{
            override fun onKeyClicked(layoutType: LayoutType, item: Item, isCapsModeOn: Boolean) {
                if(item.isCaps(layoutType)) return
                if(item.isBackSpace(layoutType)) return

                val inputConnection = currentInputConnection


                if(item.isSpace(layoutType)){
                    inputConnection.commitText(" ",1)
                    processWord(inputConnection,showNextWord = true)
                }
                else if(item.isNext(layoutType)){
                    handleEditorAction(inputConnection, currentInputEditorInfo)
                }
                else if(item.key == "."){
                    processFullStopPressed()

                }
                else {
                    inputConnection.commitText( if(isCapsModeOn) item.key.uppercase() else item.key.lowercase(), 1 )
                    if(isCapsModeOn) myKeyboard.updateCapsModeIfNeeded()
                    processWord(inputConnection)
                }
            }

            override fun onSpecialClicked(str: String, isCapsModeOn: Boolean) {
                val inputConnection = currentInputConnection

                val seq = inputConnection.getTextBeforeCursor(30, 0) ?: return

                val lastSpaceIndex = seq.lastIndexOf(" ")
                val word = seq.substring(lastSpaceIndex+1)

//        val dictionary = UserDictionary()
//        UserDictionary.Words.addWord()

                inputConnection.deleteSurroundingText( word.length, 0)
                inputConnection.commitText(
                    if(isCapsModeOn) "$str ".uppercase() else "$str ",
                    1
                )

                processWord(inputConnection, showNextWord = true)
            }

            override fun onBackSpaceClickDown() {
                startDeleting()
                //processWord(inputConnection)
            }

            override fun onBackSpaceClickUp() {
                stopDeleting()
            }

        })

    }


    private val handler:Handler = Handler(Looper.getMainLooper())
    private var runnable:Runnable? = null
    private var keepDeleting:Boolean = false

    private fun startDeleting(){
        val inputConnection = currentInputConnection
        val seq:CharSequence? = inputConnection.getSelectedText(0)
        keepDeleting = true

        if(seq.isNullOrBlank()){ // delete the last text
            inputConnection.deleteSurroundingText(1, 0)
        }
        else { // delete the selected text, i.e. replace with empty string
            inputConnection.commitText("",1)
        }

        runnable = object : Runnable{
            override fun run() {
                if(!keepDeleting) return

                inputConnection.deleteSurroundingText(1,0)
                handler.postDelayed(this, LETTER_DELETION_INTERVAL)
            }
        }

        handler.postDelayed(runnable!!, 2*LETTER_DELETION_INTERVAL) // for first time
    }

    private fun stopDeleting() {
        if(runnable != null){
            handler.removeCallbacks(runnable!!)
            runnable = null
        }
    }


    private fun processFullStopPressed(){ // trim the last space
        val inputConnection = currentInputConnection

        val seq = inputConnection.getTextBeforeCursor(5, 0)
        if(seq == null){
            inputConnection.commitText(".",1)
            return
        }

        val lastSpaceIndex = seq.lastIndexOf(" ")

        if(seq.isNotEmpty() && seq[seq.length-1] == ' ') {
            val word = seq.substring(lastSpaceIndex)

            inputConnection.deleteSurroundingText(word.length, 0)
        }

        inputConnection.commitText(". ", 1)
        myKeyboard.startSingleCaps()
        processWord(inputConnection, showNextWord = true)
    }

    private fun processWord(inputConnection: InputConnection,showNextWord:Boolean = false){
        if(showNextWord){ // select 3 probable words and show them
            myKeyboard.showSuggestion(getSuggestion(null))
            return
        }

        val seq = inputConnection.getTextBeforeCursor(30, 0) ?: return

        val lastSpaceIndex = seq.lastIndexOf(" ")

        val word = seq.substring(lastSpaceIndex+1)
        //if(word.length == 1) myKeyboard.showSuggestion(arrayOf("x","","",""))
        //if(word.length < 2) return

        println("Using for suggestion: $word")
        myKeyboard.showSuggestion( getSuggestion(word))
    }

    private fun getSuggestion(str:String?):Array<String>{
        try {
            if (str == null) {
                val random = java.util.Random()
                val i = random.nextInt(lines.size - 1)
                val j = random.nextInt(lines.size - 1)
                val k = random.nextInt(lines.size - 1)

                return arrayOf("x", lines[i], lines[j], lines[k])
            }
            val topMatches = lines
                .map { it to levenshteinDistance(str, it) }
                .sortedBy { it.second }
                .take(3)
                .map { it.first }

            return arrayOf("x", topMatches[0], topMatches[1], topMatches[2])
        }catch (_:Exception){
            return arrayOf("x","","","")
        }
    }

    private val lines = mutableListOf<String>()
    private fun readWords(){
        val inputStream = resources.openRawResource(R.raw.common_words)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        bufferedReader.useLines { lines.addAll(it) }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                        dp[i][j] = minOf(
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1,
                            dp[i - 1][j - 1] + cost
                        )
                    }
                }
            }
        }

        return dp[s1.length][s2.length]
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
