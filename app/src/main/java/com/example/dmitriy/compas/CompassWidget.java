package com.example.dmitriy.compas;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.Locale;

public class CompassWidget extends AppWidgetProvider{

    final static String LOG_TAG = "Widget Compass";
    static float degree = 0;
    static String orientationName = "";
    static Intent compassService = null;
    static int widgetUpdateTime = 1000;

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             SharedPreferences sp, int widgetID) {
        Log.d(LOG_TAG, "updateWidget " + widgetID);
        int tempUpdateTime = sp.getInt(WidgetConfigActivity.WIDGET_UPDATE_TIME + widgetID, -1);
        if (tempUpdateTime >= 0) {
            widgetUpdateTime = tempUpdateTime;
        }


        int widgetBackground = sp.getInt(WidgetConfigActivity.WIDGET_COLOR + widgetID,
                context.getResources().getColor(R.color.colorDark));
        int newDegree = sp.getInt(CompassAdapter.WIDGET_ANGLE + widgetID, -1);
        if (newDegree >= 0){
            Log.v(LOG_TAG,"newDegree = " + String.valueOf(newDegree));
            degree = newDegree;
        }
        RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.widget_compass_layout);
        widgetView.setTextViewText(R.id.angleTextView, String.format(Locale.ENGLISH, "%.0f°", degree));
        orientationName = CompassFragment.getOrientationName(degree);
        widgetView.setTextViewText(R.id.orientationTextView, orientationName);
        widgetView.setInt(R.id.widgetBackground, "setBackgroundColor", widgetBackground);
        if (CompassService.serviceStarted) {
            widgetView.setImageViewResource(R.id.updateButton, R.drawable.stop_simple_light);
        }
        else{
            widgetView.setImageViewResource(R.id.updateButton, R.drawable.start_simple_light);
        }

        Intent configIntent = new Intent(context, WidgetConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID,
                configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.settingsButton, pIntent);

        pIntent = PendingIntent.getService(context, widgetID, compassService, 0);
        widgetView.setOnClickPendingIntent(R.id.updateButton, pIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_compass_layout);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        SharedPreferences sp = context.getSharedPreferences(
                WidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        compassService = new Intent(context, CompassService.class);
        compassService.putExtra(CompassService.COMPASS_UPDATE_TIME, widgetUpdateTime);
        Log.d(LOG_TAG, "onUpdate");
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(WidgetConfigActivity.WIDGET_UPDATE_TIME + widgetID);
            editor.remove(WidgetConfigActivity.WIDGET_COLOR + widgetID);
            editor.remove(CompassAdapter.WIDGET_ANGLE + widgetID);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
        context.stopService(compassService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        if (action.equals(CompassService.ACTION)){
            degree = intent.getFloatExtra(CompassService.WIDGET_ANGLE, -1);
            //Log.v(LOG_TAG, "received angle = ");
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CompassWidget.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
