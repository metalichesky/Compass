package com.example.dmitriy.compas;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MapFragment extends Fragment implements LocationListener, View.OnClickListener {
    TextView positionTextView;
    WebView mapView;
    LocationManager locationManager;
    boolean mapLoaded;
    boolean paused;
    ImageButton updateLocationButton;

    public static String LOCALE_DATE_TIME = "dd.MM.yyyy HH:mm:ss";
    public static String LOCALE_DATE = "dd.MM.yyyy";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_map, container, false);
        updateLocationButton = view.findViewById(R.id.updateLocationButton);
        positionTextView = (TextView) view.findViewById(R.id.positionView);
        mapView= (WebView) view.findViewById(R.id.mapView);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mapLoaded = false;
        paused = false;
//        html = readFileFromAssets("DoubleGis.html");
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            createDialog();
        }
        updateLocationButton.setOnClickListener(this);
        positionTextView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = ((TextView) v.findViewById(R.id.positionView)).getText().toString();
                        text = text.replaceAll("\\(","")
                                .replaceAll("\\)","")
                                .replaceAll(",",".")
                                .replaceAll(";", ",");
                        copyToClipboard(text);
                    }
                }
        );
        return view;
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.updateLocationButton:
                sendRequestLocationUpdate();
                Toast toast = Toast.makeText(getContext(), "Поиск координат...", Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
    }

    boolean copyToClipboard(String text) {
        boolean copied = false;
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (!text.isEmpty()) {
            if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboardManager =
                        (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(text);
                copied = true;
            } else {
                android.content.ClipboardManager clipboardManager =
                        (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("TAG", text);
                clipboardManager.setPrimaryClip(clip);
                copied = true;
            }
            Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.position_copy), Toast.LENGTH_LONG);
            toast.show();
        }
        return copied;
    }



    @Override
    public void onResume(){
        super.onResume();
        paused = false;
        sendRequestLocationUpdate();
        checkEnabled();
    }

    private void sendRequestLocationUpdate(){
        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 5, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 5, 5, this);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        paused = true;
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        setPosition(location);
    }

    @Override
    public void onProviderEnabled(String provider){
        checkEnabled();
        if (getActivity() != null && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            setPosition(locationManager.getLastKnownLocation(provider));
        }
    }

    @Override
    public void onProviderDisabled(String provider){
        checkEnabled();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        if (provider.equals(LocationManager.GPS_PROVIDER)){
            Log.i("Provider GPS status:", String.valueOf(status));
        }
        else if (provider.equals(LocationManager.NETWORK_PROVIDER)){
            Log.i("Provider NET status:", String.valueOf(status));
        }
    }


    private void setPosition(Location location){
        if (location == null){
            if (!paused) {
                positionTextView.setText(getResources().getString(R.string.no_position));
            }
            return;
        }
        if (!paused){
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER) ||
                    location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
                positionTextView.setText(formatPosition(location));
            }

            if (!mapLoaded) {
                mapView.getSettings().setJavaScriptEnabled(true);
                mapView.getSettings().setDomStorageEnabled(true);
                mapView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                mapView.getSettings().setPluginState(WebSettings.PluginState.ON);
                mapView.setWebChromeClient(new WebChromeClient() {
                    public boolean onConsoleMessage(ConsoleMessage cm) {
                        Log.d(TAG, cm.message() + " -- From line "
                                + cm.lineNumber() + " of "
                                + cm.sourceId() );
                        return true;
                    }
                });
                mapView.setWebViewClient(new MyWebViewClient(location));
                mapView.addJavascriptInterface(new WebViewInterface(), "Android");
                mapView.loadUrl("file:///android_asset/DoubleGis.html");
                mapLoaded = true;
            }
            else{
                Date dateTime = Calendar.getInstance().getTime();
                mapView.evaluateJavascript(javaScriptCaller("set2GisMapMarker",
                        location.getLatitude(), location.getLongitude(), dateTime.toString()), null);
            }
        }
        addLocationToDatabase(location);
        mListener.onMapActivityDataListener(location);
    }






    @TargetApi(Build.VERSION_CODES.O)
    boolean addLocationToDatabase(Location location){
        boolean locationAdded = false;
        Date nowTime = Calendar.getInstance().getTime();
        Date lastTime;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(LOCALE_DATE_TIME);
        SimpleDateFormat dateFormat = new SimpleDateFormat(LOCALE_DATE);
        String nowTimeString = dateTimeFormat.format(nowTime);
        String nowDateString = dateFormat.format(nowTime);
        Log.v("Insert into DB","Latitude " + String.valueOf(location.getLatitude()) + "; Longitude"+ String.valueOf(location.getLongitude()));

        //location = locationFilter(location);
        Cursor cursor = TabsActivity.db.rawQuery("Select * from trackingTable order by id desc limit 1;", null);
        String lastTimeString = "";
        double lastLatitude = 0;
        double lastLongitude = 0;
        if (cursor.moveToFirst()) {
            try {
                lastTimeString = cursor.getString(cursor.getColumnIndex("time"));
                lastLatitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                lastLongitude = cursor.getDouble(cursor.getColumnIndex("longtitude"));

                lastTime = dateTimeFormat.parse(lastTimeString);
                long time1 = lastTime.getTime();
                long time2 = nowTime.getTime();
                Location lastLocation = new Location("lastLocation");
                lastLocation.setLatitude(lastLatitude);
                lastLocation.setLongitude(lastLongitude);
                double distance = location.distanceTo(lastLocation);
                double speed = calculateMovingSpeed(time1, time2, distance);

                Log.i("Speed",String.valueOf(speed));
                if (speed < 300) {
                    TabsActivity.db.execSQL("Insert into trackingTable ( 'time', 'latitude', 'longtitude') values ('"
                            + nowTimeString + "','" + String.valueOf(location.getLatitude()) + "','"
                            + String.valueOf(location.getLongitude()) + "');");
                    if (!TabsActivity.db.rawQuery("Select * from datesTable where date like '"+nowDateString+"';", null).moveToFirst()) {
                        TabsActivity.db.execSQL("Insert into datesTable ( 'date') values ('"
                                + nowDateString + "');");
                    }
                    locationAdded = true;
                    if (!paused) {
                        Toast toast = Toast.makeText(getContext(), "От предыдущей точки "
                                + String.valueOf(Math.round(distance)) + " метров\n" + "Скорость "
                                + String.valueOf(Math.round(speed)) + " км/ч", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
            catch (ParseException ex) {

            }
        }
        else {
            TabsActivity.db.execSQL("Insert into trackingTable ( 'time', 'latitude', 'longtitude') values ('"
                    + nowTimeString + "','" + String.valueOf(location.getLatitude()) + "','"
                    + String.valueOf(location.getLongitude()) + "');");
            if (!TabsActivity.db.rawQuery("Select * from datesTable where date like '"+nowDateString+"';", null).moveToFirst()) {
                TabsActivity.db.execSQL("Insert into datesTable ( 'date') values ('"
                        + nowDateString + "');");
            }
        }
        cursor = TabsActivity.db.rawQuery("Select * from datesTable;", null);
        if (cursor.moveToFirst()) {
            Log.v("Dates",cursor.getString(cursor.getColumnIndex("date")));
            while (cursor.moveToNext()) {
                Log.v("Dates",cursor.getString(cursor.getColumnIndex("date")));
            }
        }
        cursor.close();
        return locationAdded;
    }

    private double calculateMovingSpeed(long lastTime, long nowTime, double distance){
        double speed =  distance / ((nowTime - lastTime) / 3600.0);
        return speed;
    }


    private String javaScriptCaller(String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if(param instanceof String){
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

    private String formatPosition(Location location){
        String formattedString = "";
        if (location != null){
            formattedString = getResources().getString(R.string.position,location.getLatitude(),location.getLongitude());
            //Log.i("Position: ", String.valueOf(location.getLatitude()) + ";" + String.valueOf(location.getLongitude()));
        }
        return formattedString;
    }

    private void checkEnabled(){
        //Log.i("Provider GPS enabled:", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
        //Log.i("Provider NET enabled:", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
    }

    public void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Внимание")
                .setMessage("Для определения местоположения необходимо включить службу местоположения в настройках")
                .setIcon(R.drawable.tab_map_icon)
                .setCancelable(true)
                .setPositiveButton("Включить",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){
                                onClickLocationSettings();
                                //dialog.cancel();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onClickLocationSettings(){
        //Log.i("New Settings Activity:", "");
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private class MyWebViewClient extends WebViewClient {
        Location location;
        MyWebViewClient(Location location){
            this.location = location;
        }
//        @TargetApi(Build.VERSION_CODES.N)
        //@Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            //view.loadUrl(request.getUrl().toString());
//            if (url.startsWith("file"))
//            {
//                // Keep local assets in this WebView.
//                return false;
//            }
//            return true;
//        }

        @Override
        public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);

            Date dateTime = Calendar.getInstance().getTime();
            if (location != null) {
                mapView.evaluateJavascript(javaScriptCaller("create2GisMap",
                        location.getLatitude(), location.getLongitude()), null);
                mapView.evaluateJavascript(javaScriptCaller("set2GisMapMarker",
                        location.getLatitude(), location.getLongitude(), dateTime.toString()), null);
                mapLoaded = true;
            }

        }
    }

    public MapFragment(){

    }

    public interface OnMapActivityDataListener{
        void onMapActivityDataListener(Location location);
    }

    private OnMapActivityDataListener mListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof OnMapActivityDataListener){
            mListener = (OnMapActivityDataListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + "must implement OnMapActivityDataListener");
        }

    }


}
