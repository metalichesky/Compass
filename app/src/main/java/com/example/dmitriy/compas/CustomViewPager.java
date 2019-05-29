package com.example.dmitriy.compas;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class CustomViewPager extends ViewPager {
    private Boolean disable = false;
    public CustomViewPager(Context context) {
        super(context);
    }
    public CustomViewPager(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        return disable ? false : super.onTouchEvent(event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return disable ? false : super.onTouchEvent(event);
    }

    public void disableScroll(Boolean disable){
        this.disable = disable;
    }

}
