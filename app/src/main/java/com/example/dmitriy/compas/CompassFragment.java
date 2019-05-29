package com.example.dmitriy.compas;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;


public class CompassFragment extends Fragment implements SensorEventListener {

    ImageView imageView;
    TextView angleView;
    TextView orientationView;
    SensorManager sensorManager;
    float rotateDegree;
    long lastUpdate = 0;
    boolean paused = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        imageView = view.findViewById(R.id.compasView);
        angleView = view.findViewById(R.id.angleView);
        orientationView = view.findViewById(R.id.orientationView);
        if (getActivity().getSystemService(SENSOR_SERVICE) != null) {
            sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        }
        rotateDegree = 0;
        return view;
    }

@Override
    public void onStart(){
        super.onStart();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
        this.paused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.paused = true;
        sensorManager.unregisterListener(this);
    }


    //public final static String WIDGET_PREF = "widget_pref";
    //public final static String WIDGET_ANGLE = "widget_angle_";

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = - Math.round(event.values[0]);
        RotateAnimation rotateAnimation = new RotateAnimation(rotateDegree, degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(33);
        rotateDegree = degree;
        if(!paused) {
            imageView.startAnimation(rotateAnimation);
            orientationView.setText(getResources().getString(R.string.orientation, getOrientationName(event.values[0])));
            angleView.setText(getResources().getString(R.string.angle, (event.values[0])));
        }
        /*
        if (lastUpdate == 0 || (System.currentTimeMillis() - lastUpdate > 1000)) {
            lastUpdate = System.currentTimeMillis();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
            ComponentName thisAppWidget = new ComponentName(getContext().getPackageName(), CompassWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int widgetID : appWidgetIds) {
                SharedPreferences sp = getActivity().getSharedPreferences(WidgetConfigActivity.WIDGET_PREF, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(CompassAdapter.WIDGET_ANGLE + widgetID, (int) Math.abs(rotateDegree));
                editor.commit();
                CompassWidget.updateWidget(getContext(), appWidgetManager, sp, widgetID);
            }
        }
        */

    }

    public static String getOrientationName(float degree) {
        String orientationName = "";
        if(degree < 22.5 || degree > 337.5){
            orientationName = "Север";
        }
        else if(degree >= 22.5 && degree <= 67.5){
            orientationName = "Северо-Восток";
        }
        else if(degree > 67.5 && degree < 112.5){
            orientationName = "Восток";
        }
        else if(degree >= 112.5 && degree <= 157.5){
            orientationName = "Юго-Восток";
        }
        else if(degree > 157.5 && degree < 202.5){
            orientationName = "Юг";
        }
        else if(degree >= 202.5 && degree <= 247.5){
            orientationName = "Юго-Запад";
        }
        else if(degree > 247.5 && degree < 292.5){
            orientationName = "Запад";
        }
        else if(degree >= 292.5 && degree <= 337.5){
            orientationName = "Северо-Запад";
        }
        return orientationName;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        String string = "";
        switch (accuracy){
            case SENSOR_STATUS_ACCURACY_HIGH:
                string = "Высокая точность";
                break;
            case SENSOR_STATUS_ACCURACY_MEDIUM:
                string = "Средняя точность";
                break;
            case SENSOR_STATUS_ACCURACY_LOW:
                string = "Низкая точность";
                break;
            case SENSOR_STATUS_UNRELIABLE:
                string = "Ошибка сенсора";
                break;
        }
        Log.i("Точность: ", string);
    }
}