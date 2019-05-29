package com.example.dmitriy.compas;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.dmitriy.compas.TrackingFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TrackingTableFragment extends Fragment implements View.OnClickListener, TrackingFragment.OnTrackActivityDataListener {
    View view;
    Spinner dateSpinner;
    ArrayAdapter<String> adapter;

    public TrackingTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onTrackActivityDataListener(Location location){
        Log.i("Updated!","Table");
        updateDateSpinner();
        updateTableView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_tracking_table, container, false);


        Button clearButton = view.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);
        Button updateButton = view.findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);
        dateSpinner = view.findViewById(R.id.dateSpinner);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTableView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dateSpinner.setAdapter(adapter);

        updateDateSpinner();
        updateTableView();
        return view;
    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.clearButton:
                clearTable();
                updateDateSpinner();
                updateTableView();
                break;
            case R.id.updateButton:

                updateDateSpinner();
                updateTableView();
                break;
        }
    }

    void clearTable(){
        TabsActivity.db.execSQL("Delete from trackingTable;");
        TabsActivity.db.execSQL("Delete from datesTable;");
        adapter.clear();
        Log.v("Execute query", "Deleting");
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



    void updateTableView(){
        try {
            TableLayout tableLayout = view.findViewById(R.id.trackingTable);
            tableLayout.removeAllViews();
            tableLayout.setShrinkAllColumns(true);
            tableLayout.setStretchAllColumns(true);


            TableRow row = new TableRow(this.getActivity());
            TextView time = new TextView(this.getActivity());
            TextView latitude = new TextView(this.getActivity());
            TextView longitude = new TextView(this.getActivity());

            time.setText("Время");
            latitude.setText("Широта");
            longitude.setText("Долгота");
            time.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            latitude.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            longitude.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            time.setTextColor(getResources().getColor(R.color.colorPrimary));
            latitude.setTextColor(getResources().getColor(R.color.colorPrimary));
            longitude.setTextColor(getResources().getColor(R.color.colorPrimary));
            time.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
            latitude.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
            longitude.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
            time.setMinHeight(50);
            latitude.setMinHeight(50);
            longitude.setMinHeight(50);
            row.addView(time);
            row.addView(latitude);
            row.addView(longitude);
            row.setGravity(Gravity.CENTER);
            tableLayout.addView(row);


            Cursor cursor;
            cursor = TabsActivity.db.rawQuery("Select * from trackingTable", null); //Выполняем запрос из базы
            if (cursor.moveToFirst()) { //переходим на первый элемент если он есть
                do {
                    cursor.getString(cursor.getColumnIndex("time"));

                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(MapFragment.LOCALE_DATE_TIME);
                    SimpleDateFormat dateFormat = new SimpleDateFormat(MapFragment.LOCALE_DATE);
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
                        row = new TableRow(this.getActivity());
                        //TextView id = new TextView(this.getActivity());
                        time = new TextView(this.getActivity());
                        latitude = new TextView(this.getActivity());
                        longitude = new TextView(this.getActivity());
                        //id.setText(cursor.getString(cursor.getColumnIndex("id")));
                        time.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        latitude.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        longitude.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        time.setText(cursor.getString(cursor.getColumnIndex("time")));
                        latitude.setText(cursor.getString(cursor.getColumnIndex("latitude")));
                        longitude.setText(cursor.getString(cursor.getColumnIndex("longtitude")));
                        time.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
                        latitude.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
                        longitude.setBackground(getResources().getDrawable(R.drawable.tracking_list_background));
                        time.setMinHeight(50);
                        latitude.setMinHeight(50);
                        longitude.setMinHeight(50);
                        time.setTextColor(getResources().getColor(R.color.colorPrimary));
                        latitude.setTextColor(getResources().getColor(R.color.colorPrimary));
                        longitude.setTextColor(getResources().getColor(R.color.colorPrimary));

                        //row.addView(id);
                        row.addView(time);
                        row.addView(latitude);
                        row.addView(longitude);
                        row.setGravity(Gravity.CENTER);
                        row.setMinimumHeight(50);
                        tableLayout.addView(row);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch(NullPointerException ex){
            Log.e("Error updating table",ex.toString());
        }
    }

}
