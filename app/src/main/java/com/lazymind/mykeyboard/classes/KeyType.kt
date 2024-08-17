package com.lazymind.mykeyboard.classes
import com.lazymind.mykeyboard.classes.LayoutType.*

enum class KeyType(val layoutType: LayoutType, val x:Int, val y:Int, val weight:Int=1){
    OPTIONS(COMMON,0,0), STICKER(COMMON,0,1), GIF(COMMON,0,2), CLIPBOARD(COMMON,0,3), THEME(COMMON,0,4), MIC(COMMON,0,5),

    CAPS(MAIN,3,0), BACKSPACE(COMMON,3,8),

    CHAR_DIG(COMMON,4,0), SPACE(COMMON,4,2,5), NEXT(COMMON,4,4), NORMAL(COMMON,-1,-1),

    DIG_SYMBOL(SECONDARY, 4,0)
}
