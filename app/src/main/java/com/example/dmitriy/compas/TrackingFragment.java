package com.example.dmitriy.compas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.time.LocalDateTime;


public class TrackingFragment extends Fragment implements TabsActivity.OnActivityDataListener {

    View trackingView;
    MyFragmentPagerAdapter pagerAdapter;

    interface OnTrackActivityDataListener{
        void onTrackActivityDataListener(Location location);
    }
    private OnTrackActivityDataListener mListener1, mListener2;

    public TrackingFragment(){
    }

    @Override
    public void onActivityDataListener(Location location){
        if (mListener1 != null) {
            mListener1.onTrackActivityDataListener(location);
        }
        if (mListener2 != null) {
            mListener2.onTrackActivityDataListener(location);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        trackingView = inflater.inflate(R.layout.activity_tracking, container, false);
        CustomViewPager pagerView = trackingView.findViewById(R.id.trackingPagerView);
        setupPagerAdapter();
        pagerView.setAdapter(pagerAdapter);
        pagerView.disableScroll(true);
        TabLayout tabLayout = trackingView.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(pagerView);

        return trackingView;
    }

    public void setupPagerAdapter(){
        Fragment tableFragment = new TrackingTableFragment();
        Fragment mapFragment = new TrackingMapFragment();
        pagerAdapter = new MyFragmentPagerAdapter( getChildFragmentManager(), getActivity());
        pagerAdapter.addFragment(tableFragment, getResources().getString(R.string.table), R.drawable.tab_map_icon);
        pagerAdapter.addFragment(mapFragment, getResources().getString(R.string.map), R.drawable.tab_compass_icon);

        if (mapFragment instanceof OnTrackActivityDataListener) {
            mListener1 = (OnTrackActivityDataListener) mapFragment;
        }
        if (tableFragment instanceof OnTrackActivityDataListener) {
            mListener2 = (OnTrackActivityDataListener) tableFragment;
        }

    }




}