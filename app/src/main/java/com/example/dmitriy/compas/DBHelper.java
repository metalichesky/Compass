package com.example.dmitriy.compas;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "DBHelper";
    private ArrayList<String[]> tablesArray;



    public DBHelper(Context context) {
        super(context, "myDB", null, 1); //myDB – имя базы данных
        tablesArray = new ArrayList<>();
    }

    public boolean addTable(String[] newTable){
        Log.d(LOG_TAG, "addTable");
        boolean tableAdded = false;
        if (findTable(newTable[0]) < 0) {
            tablesArray.add(newTable);
            tableAdded = true;
        }
        return tableAdded;
    }

    public int findTable(String tableName){
        int tablePosition = -1;
        for(int i = 0; i < tablesArray.size(); i++){
            String[] tableData = tablesArray.get(i);
            if (tableData[0].equals(tableName)) {
                tablePosition = i;
            }
        }
        return tablePosition;
    }

    public void removeTables(){
        Log.d(LOG_TAG, "removeTables");
        tablesArray.clear();
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");
        createTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");
        dropTables(db);
        createTables(db);
    }

    public void dropTables(SQLiteDatabase db){
        for(String[] tableData: tablesArray){
            StringBuilder query = new StringBuilder();
            query.append("drop table if exist ");
            query.append(tableData[0]);
            query.append(";");
            db.execSQL(query.toString());
        }
    }

    public void createTables(SQLiteDatabase db){
        for (String[] tableData: tablesArray){
            StringBuilder query = new StringBuilder();
            query.append("create table ").append(tableData[0]).append("(");
            String divider = ",";
            for (int i = 1; i < tableData.length; i++){
                query.append(tableData[i]);
                query.append(divider);
            }
            query.deleteCharAt(query.lastIndexOf(divider));
            //query.replace(query.lastIndexOf(divider), divider.length(), "");
            query.append(");");
            db.execSQL(query.toString());
        }
    }

    public String printTable(){
        StringBuilder tables = new StringBuilder();
        for (String[] tableData: tablesArray){
            tables.append("Table Name: ").append(tableData[0]).append("\n").append("Rows: ");
            for (int i = 1; i < tableData.length; i++){
                tables.append(tableData[i]).append(", ");
            }
            tables.append("\n");
        }
        return tables.toString();
    }
}