package com.example.dmitriy.compas;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

public class CompassAdapter extends Activity implements SensorEventListener {
        double DELTA = 0.000000001;
        SensorManager sensorManager;
        int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent resultValue;
        double degree;
        int sensorCounter = 0;
        final String LOG_TAG = "myLogs";

        public final static String WIDGET_PREF = "widget_pref";
        public final static String WIDGET_ANGLE = "widget_angle_";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(LOG_TAG, "onCreate compass adapter");
            moveTaskToBack(true);
            // извлекаем ID конфигурируемого виджета
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            // и проверяем его корректность
            if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }

            // формируем intent ответа
            resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

            // отрицательный ответ
            setResult(RESULT_CANCELED, resultValue);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
            else finish();
        }



    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorCounter++;
        degree = event.values[0];


        if (Math.abs(degree) > DELTA && sensorCounter > 10){
            sensorCounter = 0;
            Log.d(LOG_TAG, "degree value " + String.valueOf(degree));
            SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(WIDGET_ANGLE + widgetID, (int) degree);
            editor.commit();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            CompassWidget.updateWidget(this, appWidgetManager, sp, widgetID);

            // положительный ответ
            setResult(RESULT_OK, resultValue);

            Log.d(LOG_TAG, "finish adapter " + widgetID + "with value " + String.valueOf(degree));
            sensorManager.unregisterListener(this);
            finish();
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
        Log.i("Точность: ", string);
    }
}
