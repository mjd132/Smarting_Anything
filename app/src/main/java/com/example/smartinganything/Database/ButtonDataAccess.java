package com.example.smartinganything.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartinganything.ViewModels.ButtonViewModel;

import java.util.ArrayList;
import java.util.List;

/*
 * Data Access for do operation CRUD to database for Buttons table
 */
public class ButtonDataAccess {

    public static List<ButtonViewModel> buttonDataList = new ArrayList<>();
    public static List<ButtonViewModel> buttonsWaitingResponseMessage = new ArrayList<>();
    Context context;
    private final SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase database;

    public ButtonDataAccess(Context context) {
        this.context = context;
        sqLiteOpenHelper = new SqliteDB(context);
        buttonDataList = this.getAllButtons();
    }

    public void openDB() {
        database = sqLiteOpenHelper.getWritableDatabase();
        Log.d(SqliteDB.TAG, "Database Opened!");
    }

    public void closeDB() {
        database.close();
        Log.d(SqliteDB.TAG, "Database Closed!");
    }

    public long addNewButton(ButtonViewModel buttonViewModel) {

        try {
            openDB();
            String nameButton = buttonViewModel.getName();
            String stateButton = buttonViewModel.getState();
            int backgroundColor = buttonViewModel.getBackgroundColor();
            int action_id = buttonViewModel.action.id;

            ContentValues values = new ContentValues();
            values.put(SqliteDB.BUTTON_NAME, nameButton);
            values.put(SqliteDB.BUTTON_STATE, stateButton);
            values.put(SqliteDB.BUTTON_BACKGROUND_COLOR, backgroundColor);
            values.put(SqliteDB.ACTION_ID, action_id);

            long record = database.insert(SqliteDB.TABLE_BUTTONS, null, values);

            Log.d(SqliteDB.TAG, "Button Adding...\n result code:" + record);
            closeDB();
            updateList();
            return record;

        } catch (Exception e) {
            Log.d(SqliteDB.TAG, "ADD BUTTON:", e);
            closeDB();
            return -1;
        }
    }

    @SuppressLint("Range")
    public List<ButtonViewModel> getAllButtons() {
        openDB();
        List<ButtonViewModel> buttonViewModels = new ArrayList<>();
        String getAllQuery = "select * from " + SqliteDB.TABLE_BUTTONS;

        Cursor cursor = database.rawQuery(getAllQuery, null);
        if (cursor == null) {
            closeDB();
            return buttonViewModels;
        }
        if (cursor.moveToFirst()) {
            try {

                do {
                    int id = cursor.getInt(cursor.getColumnIndex(SqliteDB.B_ID));
                    String name = cursor.getString(cursor.getColumnIndex(SqliteDB.BUTTON_NAME));
                    String state = cursor.getString(cursor.getColumnIndex(SqliteDB.BUTTON_STATE));
                    int bgColor = cursor.getInt(cursor.getColumnIndex(SqliteDB.BUTTON_BACKGROUND_COLOR));
                    int action_id = cursor.getInt(cursor.getColumnIndex(SqliteDB.ACTION_ID));


                    buttonViewModels.add(new ButtonViewModel(context, id, name, state, bgColor, action_id));


                } while (cursor.moveToNext());
                Log.d(SqliteDB.TAG, "Button Read!");
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
        return buttonViewModels;
    }

    public boolean deleteButton(ButtonViewModel buttonViewModel) {
        openDB();
        int id = buttonViewModel.id;
        try {
            database.delete(SqliteDB.TABLE_BUTTONS, SqliteDB.B_ID + "=?", new String[]{String.valueOf(id)});
            Log.d(SqliteDB.TAG, "Button Deleted!");
        } catch (Exception e) {
            Log.e(SqliteDB.TAG, "Exception on delete button: ", e);
            closeDB();
            return false;
        }
        closeDB();
        updateList();
        return true;
    }

    public boolean updateButton(ButtonViewModel newButton) {
        int id = newButton.id;
        ContentValues values = new ContentValues();
        values.put(SqliteDB.BUTTON_NAME, newButton.name);
        values.put(SqliteDB.BUTTON_BACKGROUND_COLOR, newButton.backgroundColor);
        values.put(SqliteDB.BUTTON_STATE, newButton.state);
        openDB();
        try {
            database.update(SqliteDB.TABLE_BUTTONS, values, SqliteDB.B_ID + "=?", new String[]{String.valueOf(id)});
            Log.d(SqliteDB.TAG, "Button Updated!");
        } catch (Exception e) {
            Log.e(SqliteDB.TAG, "Exception in Update Button Table: ", e);
            closeDB();
            return false;
        }
        updateList();
        closeDB();
        return true;
    }

    void updateList() {
        buttonDataList = getAllButtons();
    }

    public long getLastRowID() {
        openDB();
        Cursor cursor = database.rawQuery("SELECT last_insert_rowid()", null);
        cursor.moveToFirst();
        long lastRowId = cursor.getLong(0);
        cursor.close();
        closeDB();
        return lastRowId;
    }
}
