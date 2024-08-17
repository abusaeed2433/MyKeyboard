package com.lazymind.mykeyboard.classes;

import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class SpecialRow extends Row{

    private static SpecialRow instance = null;
    public final Paint paint;
    private final float textSizeRatio; // 1sp = textSizeRatio dp

    public final String[] suggestions;

    public static SpecialRow getInstance(float textSizeRatio, int rowNo, @NonNull ArrayList<Item> items, @NonNull GapType gapType){
        if(instance == null){
            instance = new SpecialRow(textSizeRatio,rowNo, items, gapType);
        }

        return instance;
    }

    private SpecialRow(float textSizeRatio, int rowNo, @NonNull ArrayList<Item> items, @NonNull GapType gapType) {
        super(rowNo, items, gapType);
        this.textSizeRatio = textSizeRatio;

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTextSize(textSizeRatio*20f); // 20sp
        suggestions = new String[]{"x","-","-","-"};
    }

    public void update(String[] words){
        System.arraycopy(words, 0, suggestions, 0, words.length);
    }

}
