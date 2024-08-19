package com.lazymind.mykeyboard.classes;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SpecialRow extends Row{

    private static final float LINE_HEIGHT = .5f;

    private static SpecialRow instance = null;
    public final Paint textPaint;
    public final Paint backPaint;
    private final float textSizeRatio; // 1sp = textSizeRatio dp

    public final String[] suggestions;
    private float lineTopPoint = -1f;
    private float lineBottomPoint = -1f;

    public static SpecialRow getInstance(float textSizeRatio, int rowNo, @NonNull ArrayList<Item> items, @NonNull GapType gapType){
        if(instance == null){
            instance = new SpecialRow(textSizeRatio,rowNo, items, gapType);
        }

        return instance;
    }

    private SpecialRow(float textSizeRatio, int rowNo, @NonNull ArrayList<Item> items, @NonNull GapType gapType) {
        super(rowNo, items, gapType);
        this.textSizeRatio = textSizeRatio;

        textPaint = new Paint();
        backPaint = new Paint();

        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setTextSize(textSizeRatio*20f); // 20sp

        //backPaint.setColor(Color.rgb(189, 234, 240));
        backPaint.setColor(Color.BLACK);

        suggestions = new String[]{"x","-","-","-"};
    }

    public float getLineTopPoint(){
        if(lineTopPoint == -1f){
            lineTopPoint = get(0).getBaseRect().top + get(0).getBaseRect().height() * (1 - LINE_HEIGHT)/2f;
        }

        System.out.println("bottom point: "+lineTopPoint);
        return lineTopPoint;
    }

    public float getLineBottomPoint(){
        if(lineBottomPoint == -1f){
            lineBottomPoint = get(0).getBaseRect().bottom - get(0).getBaseRect().height() * (1 - LINE_HEIGHT)/2f;
            System.out.println("Calc point: "+get(0).getBaseRect().bottom +", "+get(0).getBaseRect().height());
        }

        System.out.println("bottom point: "+lineBottomPoint);
        return lineBottomPoint;
    }

    public void update(String[] words){
        Rect bounds = new Rect();
        final float minTextSize = 8f;
        float textSize = 20f * textSizeRatio; // 20sp

        textPaint.setTextSize(textSize);
        for(int i=0; i<words.length; i++){
            suggestions[i] = words[i];
            float textWidth = textPaint.measureText(words[i]);
            textPaint.getTextBounds(words[i], 0, words[i].length(), bounds);
            float textHeight = bounds.height() - 2*bounds.bottom;

            if(textWidth >= get(i).getBorderRect().width()){
                textSize = Math.max(textSize - textSize * .2f, minTextSize); // 20% reduces
                textPaint.setTextSize(textSize);
            }

            get(i).updateTextWidthHeight(textWidth, textHeight);
        }
    }

}
