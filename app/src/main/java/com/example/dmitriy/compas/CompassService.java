package com.example.dmitriy.compas;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

public class CompassService extends Service implements SensorEventListener {
    public static final String ACTION = "com.example.dmitriy.compas.UPDATE_COMPASS_ANGLE";
    public static final String WIDGET_ANGLE = "widget_angle_";
    public static final String COMPASS_UPDATE_TIME = "widget_angle_";
    public static boolean serviceStarted = false;
    SensorManager sensorManager;
    float rotateDegree = 0;
    long lastUpdate = 0;
    int updateTime = 1000;


    @Override
    public IBinder onBind(Intent intent){
        return null;

    }

    @Override
    public void onCreate(){
        super.onCreate();
        //Toast.makeText(this, "Служба создана", Toast.LENGTH_SHORT);
        Log.v("Service Compass","created");
        if (getSystemService(SENSOR_SERVICE) != null) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
        if (serviceStarted){
            serviceStarted = false;
            Log.v("Service Compass","stopped");
            stopSelf();
        }
        else{
            serviceStarted = true;
            Log.v("Service Compass","started");
            if (intent.getExtras() != null){
                Log.v("Service Compass","has extras");
                updateTime = intent.getIntExtra(COMPASS_UPDATE_TIME, 1000);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        Log.v("Service Compass","destroyed");
        sensorManager.unregisterListener(this);
        serviceStarted = false;
        Intent intent = new Intent(ACTION);
        intent.putExtra(WIDGET_ANGLE, Math.abs(rotateDegree));
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = - Math.round(event.values[0]);
        RotateAnimation rotateAnimation = new RotateAnimation(rotateDegree, degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(33);
        rotateDegree = degree;
        if (lastUpdate == 0 || (System.currentTimeMillis() - lastUpdate > updateTime)) {
            //Log.v("Service Compass","new angle value");
            lastUpdate = System.currentTimeMillis();
            Intent intent = new Intent(ACTION);
            intent.putExtra(WIDGET_ANGLE, Math.abs(rotateDegree));
            sendBroadcast(intent);
        }
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
        //Log.i("Точность: ", string);
    }


}
