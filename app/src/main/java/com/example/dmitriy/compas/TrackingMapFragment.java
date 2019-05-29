package com.example.dmitriy.compas;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class TrackingMapFragment extends Fragment implements TrackingFragment.OnTrackActivityDataListener {
    private View view;
    private WebView mapView;
    boolean mapLoaded;
    Location location;
    Spinner dateSpinner;
    ArrayAdapter<String> adapter;

    public TrackingMapFragment() {
        // Required empty public constructor
    }
    @Override
    public void onTrackActivityDataListener(Location location){
        this.location = location;
        updateDateSpinner();
        drawTracking();
    }

    private String getLocationsFromDatabase(){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Cursor cursor;
            cursor = TabsActivity.db.rawQuery("Select * from trackingTable", null); //Выполняем запрос из базы
            stringBuilder.append("[");
            if (cursor.moveToFirst()) { //переходим на первый элемент если он есть
                do{
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String selectedDate = "";
                    Date viewDate = new Date();
                    try {
                        viewDate = dateTimeFormat.parse(cursor.getString(cursor.getColumnIndex("time")));
                        dateFormat.format(viewDate);
                        selectedDate = (String)dateSpinner.getSelectedItem();
                    }
                    catch (ParseException | IndexOutOfBoundsException ex2){
                        break;
                    }
                    if (selectedDate.equals(dateFormat.format(viewDate))) {
                        stringBuilder.append("[");
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex("latitude")));
                        stringBuilder.append(", ");
                        stringBuilder.append(cursor.getString(cursor.getColumnIndex("longtitude")));
                        stringBuilder.append("],");
                    }
                }while(cursor.moveToNext());
            }
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            stringBuilder.append("]");
            cursor.close();
        }
        catch(NullPointerException ex){
            Log.e("Error get coord", ex.toString());
        }
        return stringBuilder.toString();
    }

    private void drawTracking(){
            if (!mapLoaded && mapView != null) {

                mapView.getSettings().setJavaScriptEnabled(true);
                mapView.getSettings().setDomStorageEnabled(true);
                mapView.getSettings().setPluginState(WebSettings.PluginState.ON);
                mapView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                mapView.setWebChromeClient(new WebChromeClient() {
                    public boolean onConsoleMessage(ConsoleMessage cm) {
                        Log.d(TAG, cm.message() + " -- From line "
                                + cm.lineNumber() + " of "
                                + cm.sourceId() );
                        return true;
                    }
                });
                mapView.setWebViewClient(new MyWebViewClient());
                mapView.addJavascriptInterface(new WebViewInterface(), "Android");
                mapView.loadUrl("file:///android_asset/DoubleGis.html");
            }
            else if (mapView != null){
                //Date dateTime = Calendar.getInstance().getTime();

                //Log.i("Drawing","Draw track");

                String coordinates = getLocationsFromDatabase();
                mapView.evaluateJavascript(javaScriptCaller("set2GisMapTrack",
                        coordinates), null);
            }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_tracking_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        dateSpinner = view.findViewById(R.id.dateSpinner);


        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                drawTracking();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dateSpinner.setAdapter(adapter);

        updateDateSpinner();
        drawTracking();
        return view;
    }

    void updateDateSpinner() {
        try {

            Cursor cursor;
            cursor = TabsActivity.db.rawQuery("Select * from datesTable;", null);
            if (cursor.moveToFirst()) {
                adapter.clear();
                Log.v("Dates",cursor.getString(cursor.getColumnIndex("date")));
                adapter.add(cursor.getString(cursor.getColumnIndex("date")));
                while (cursor.moveToNext()) {
                    Log.v("Dates",cursor.getString(cursor.getColumnIndex("date")));
                    adapter.add(cursor.getString(cursor.getColumnIndex("date")));
                }
            }
            cursor.close();
            //dateSpinner.setPrompt("Title");

        }
        catch(NullPointerException ex){
            Log.e("Exception",ex.toString());
        }
    }

    private String javaScriptCaller(String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if(param instanceof String && !((String) param).startsWith("[")){
                stringBuilder.append("'");
                stringBuilder.append(param);
                stringBuilder.append("'");
            }
            else{
                stringBuilder.append(param.toString());
            }
            if(i < params.length - 1){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")}catch(error){Android.onError(error.message);}");
        return stringBuilder.toString();
    }

    private class WebViewInterface{
        @JavascriptInterface
        public void onError(String error){
            throw new Error(error);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        MyWebViewClient(){

        }

        @Override
        public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);
            //Log.i("Drawing","Finished, init map");
            try {

                mapView.evaluateJavascript(javaScriptCaller("create2GisMap",
                        0, 0), null);
                //Log.i("Drawing","Draw track");
                String coordinates = getLocationsFromDatabase();
                //Log.i("Drawing coordinates", javaScriptCaller("set2GisMapTrack",coordinates));
                mapView.evaluateJavascript(javaScriptCaller("set2GisMapTrack",
                        coordinates), null);
                mapLoaded = true;
            }
            catch(NullPointerException ex) {
            }
        }
    }
}
