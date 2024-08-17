package com.lazymind.mykeyboard.classes

enum class KeyType(val x:Int, val y:Int, val weight:Int=1){
    OPTIONS(0,0), STICKER(0,1), GIF(0,2), CLIPBOARD(0,3), THEME(0,4), MIC(0,5),

    CAPS(3,0), BACKSPACE(3,8),

    CHAR_DIG(4,0), SPACE(4,2,5), NEXT(4,4), NORMAL(-1,-1);
}
