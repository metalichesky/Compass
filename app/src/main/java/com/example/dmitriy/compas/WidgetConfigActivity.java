package com.example.dmitriy.compas;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class WidgetConfigActivity extends Activity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    final String LOG_TAG = "myLogs";

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_UPDATE_TIME = "widget_update_time_";
    public final static String WIDGET_COLOR = "widget_color_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate config");

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

        setContentView(R.layout.widget_config);
    }


    public void onClick(View v) {
        int selRBColor = ((RadioGroup) findViewById(R.id.rgColor))
                .getCheckedRadioButtonId();
        int color = Color.RED;
        switch (selRBColor) {
            case R.id.radioTransparent:
                color = getResources().getColor(R.color.colorTransparent);
                break;
            case R.id.radioDark:
                color = getResources().getColor(R.color.colorDark);
                break;
            case R.id.radioBlack:
                color = getResources().getColor(R.color.colorDarkBackground);
                break;
        }
        EditText etText = (EditText) findViewById(R.id.etText);

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        Editor editor = sp.edit();

        String updateTimeString = etText.getText().toString();
        int updateTime = 0;
        if (!updateTimeString.isEmpty() && Integer.parseInt(updateTimeString) > 0){
            updateTime = Integer.parseInt(updateTimeString);
        }
        else{
            Toast.makeText(this, "Введите время правильно", Toast.LENGTH_SHORT).show();
            return;
        }

        editor.putInt(WIDGET_UPDATE_TIME + widgetID, updateTime);
        editor.putInt(WIDGET_COLOR + widgetID, color);
        editor.commit();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        CompassWidget.updateWidget(this, appWidgetManager, sp, widgetID);
        // положительный ответ
        setResult(RESULT_OK, resultValue);



        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
    }
}