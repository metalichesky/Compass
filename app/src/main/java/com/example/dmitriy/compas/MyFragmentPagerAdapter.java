package com.example.dmitriy.compas;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    private final List <Fragment> fragmentList = new ArrayList<Fragment>();
    private final List<String> tabTitleList = new ArrayList<String>();
    private final List<Integer> tabImageList = new ArrayList<Integer>();

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }


    public void addFragment(Fragment fragment, String title, int imageResourceId){
        fragmentList.add(fragment);
        tabTitleList.add(title);
        tabImageList.add(imageResourceId);
    }

    public Fragment getFragment(String title){
        int indexOfFragment = tabTitleList.indexOf(title);
        if (indexOfFragment >= 0){
            return fragmentList.get(indexOfFragment);
        }
        else {
            return null;
        }
    }

    @Override public int getCount() {
        return fragmentList.size();
    }

    @Override public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override public CharSequence getPageTitle(int position) {
        Drawable image = context.getResources().getDrawable(tabImageList.get(position));
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString("   " + tabTitleList.get(position));
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}