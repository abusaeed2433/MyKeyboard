package com.lazymind.mykeyboard.classes
import com.lazymind.mykeyboard.classes.LayoutType.*


enum class KeyType(val x:Int, val y:Int, val weight:Int, vararg val layoutTypes: LayoutType, ){

    OPTIONS(0,0,1,COMMON),
    STICKER(0,1,1, COMMON),
    GIF(0,2, 1, COMMON),
    CLIPBOARD(0,3,1,COMMON),
    THEME(0,4,1,COMMON),
    MIC(0,5,1,COMMON),

    CAPS(3,0,1,MAIN),
    BACKSPACE(3,8,1,COMMON),

    CHAR_DIG(4,0,1, COMMON, MAIN, DIGIT_SYMBOL),
    SPACE(4,2,5,COMMON),
    NEXT(4,4,1,COMMON),
    NORMAL(-1,-1,1,COMMON),

    SYMBOL_SWITCH( 3,0,1,DIGIT_SYMBOL,SYMBOL),

    CANCEL_PREVIEW(0,0,1,TOP_ROW),
    SUGGEST_ONE(0,1,3,TOP_ROW),
    SUGGEST_TWO(0,2,3,TOP_ROW),
    SUGGEST_THREE(0,3,3,TOP_ROW);

    fun doesHave(type: LayoutType):Boolean {
        for(layoutType in layoutTypes){
            if(layoutType == COMMON) return true

            if(layoutType == type) return true
        }
        return false
    }

}
