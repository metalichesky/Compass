package com.example.dmitriy.compas;

import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class TabsActivity extends AppCompatActivity implements MapFragment.OnMapActivityDataListener{
    MyFragmentPagerAdapter pagerAdapter;

    public static DBHelper dbHelper;
    public static SQLiteDatabase db;

    interface OnActivityDataListener{
        void onActivityDataListener(Location location);
    }
    private OnActivityDataListener mListener;

    @Override
    public void onMapActivityDataListener(Location location){
        mListener.onActivityDataListener(location);
    }

    private void setOnActivityDataListener(Fragment fragment){
        if (fragment instanceof OnActivityDataListener) {
            mListener = (OnActivityDataListener) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        CustomViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(
                new MyFragmentPagerAdapter(getSupportFragmentManager(), TabsActivity.this)
        );
        setupViewPager(viewPager);
        viewPager.disableScroll(true);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        Fragment fragment = pagerAdapter.getFragment(getResources().getString(R.string.text_tab3));
        if (fragment != null){
            setOnActivityDataListener(fragment);
        }
        initDatabases();


    }

    void initDatabases(){
        dbHelper = new DBHelper(this);
        dbHelper.addTable(
                new String[]{
                        "trackingTable",
                        "id integer primary key autoincrement",
                        "time text",
                        "latitude text",
                        "longtitude text"
                });
        dbHelper.addTable(
                new String[]{
                        "datesTable",
                        "date text not null unique"
                });

        Log.v("Database:",dbHelper.printTable());
        db = dbHelper.getWritableDatabase();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        dbHelper.close();
    }


    public void setupViewPager(CustomViewPager viewPager){

        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), this);
        pagerAdapter.addFragment(new CompassFragment(), getResources().getString(R.string.text_tab1), R.drawable.tab_compass_icon);
        pagerAdapter.addFragment(new MapFragment(), getResources().getString(R.string.text_tab2), R.drawable.tab_map_icon);
        pagerAdapter.addFragment(new TrackingFragment(), getResources().getString(R.string.text_tab3), R.drawable.tab_tracking_icon);
        viewPager.setAdapter(pagerAdapter);
    }
}



