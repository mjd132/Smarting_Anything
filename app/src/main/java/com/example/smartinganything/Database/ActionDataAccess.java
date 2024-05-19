package com.example.smartinganything.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartinganything.ViewModels.Action;

import java.util.ArrayList;
import java.util.List;

public class ActionDataAccess {

    public List<Action> actionList = new ArrayList<>();
    private final SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase database;


    public ActionDataAccess(Context context) {

        sqLiteOpenHelper = new SqliteDB(context);
        actionList = getAllActions();
    }

    public void openDB() {
        database = sqLiteOpenHelper.getWritableDatabase();
        Log.d(SqliteDB.TAG, "Database Opened!");
    }

    public void closeDB() {
        database.close();
        Log.d(SqliteDB.TAG, "Database Closed!");
    }

    @SuppressLint("Range")
    public List<Action> getAllActions() {
        List<Action> actions = new ArrayList<>();
        String getAllQuery = "select * from " + SqliteDB.TABLE_ACTIONS;
        openDB();
        Cursor cursor = database.rawQuery(getAllQuery, null);
        if (cursor == null) {
            closeDB();
            return new ArrayList<>();
        }
        if (cursor.moveToFirst()) {
            try {

                do {
                    int id = cursor.getInt(cursor.getColumnIndex(SqliteDB.A_ID));
                    String name = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_NAME));
                    String onMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_ON_MESSAGE));
                    String offMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_OFF_MESSAGE));
                    String rMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_R_MESSAGE));

                    actions.add(new Action(id, name, rMessage, onMessage, offMessage));

                } while (cursor.moveToNext());
                Log.d(SqliteDB.TAG, "Action Read!");
            } catch (Exception e) {
                Log.e(SqliteDB.TAG, "Exception in get all button: ", e);
                closeDB();
                cursor.close();
            } finally {

                cursor.close();

            }

        }
        cursor.close();
        closeDB();
        return actions;

    }

    @SuppressLint("Range")
    public Action getAction(int id) {
        Action action = new Action();
        openDB();
        Cursor cursor = database.query(SqliteDB.TABLE_ACTIONS, null, SqliteDB.A_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int a_id = cursor.getInt(cursor.getColumnIndex(SqliteDB.A_ID));
                String name = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_NAME));
                String onMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_ON_MESSAGE));
                String offMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_OFF_MESSAGE));
                String rMessage = cursor.getString(cursor.getColumnIndex(SqliteDB.ACTION_R_MESSAGE));

                action = new Action(a_id, name, rMessage, onMessage, offMessage);


            } catch (Exception e) {
                cursor.close();
                closeDB();
                return null;
            }

        }
        cursor.close();
        closeDB();
        return action;
    }

    public long createAction(Action action) {

        try {
            openDB();
            String name = action.getName();
            String offMessage = action.getBtOffMSG();
            String onMessage = action.getBtOnMSG();
            String rMessage = action.getBtRMSG();


            ContentValues values = new ContentValues();
            values.put(SqliteDB.ACTION_NAME, name);
            values.put(SqliteDB.ACTION_ON_MESSAGE, onMessage);
            values.put(SqliteDB.ACTION_OFF_MESSAGE, offMessage);
            values.put(SqliteDB.ACTION_R_MESSAGE, rMessage);

            long record = database.insert(SqliteDB.TABLE_ACTIONS, null, values);

            Log.d(SqliteDB.TAG, "Action Adding...\n result code:" + record);
            closeDB();
            updateList();
            return record;

        } catch (Exception e) {
            Log.d(SqliteDB.TAG, "ADD BUTTON:", e);
            closeDB();
            return -1;
        }
    }

    private void updateList() {
        actionList = getAllActions();
    }

    public int updateAction(Action action) {
        int id = action.id;
        ContentValues values = new ContentValues();
        values.put(SqliteDB.ACTION_NAME, action.name);
        values.put(SqliteDB.ACTION_R_MESSAGE, action.btRMSG);
        values.put(SqliteDB.ACTION_ON_MESSAGE, action.btOnMSG);
        values.put(SqliteDB.ACTION_OFF_MESSAGE, action.btOffMSG);
        int row;
        openDB();
        try {
            row = database.update(SqliteDB.TABLE_ACTIONS, values, SqliteDB.A_ID + "=?", new String[]{String.valueOf(id)});
            Log.d(SqliteDB.TAG, "Action Updated!");
        } catch (Exception e) {
            Log.e(SqliteDB.TAG, "Exception in Update Action Table: ", e);
            closeDB();
            return -1;
        }
        updateList();
        closeDB();
        return row;
    }

    @SuppressLint("Range")
    public int getLastID(long rowId) {

        int id = -1;
        openDB();
        try {
            Cursor cursor = database.query(SqliteDB.TABLE_ACTIONS, new String[]{SqliteDB.A_ID}, "_ROWID_ = ?", new String[]{String.valueOf(rowId)}, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(SqliteDB.A_ID));

            }
            cursor.close();
        } catch (Exception e) {
            return -1;
        }
        closeDB();
        return id;
    }

}
