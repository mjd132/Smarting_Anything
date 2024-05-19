package com.example.smartinganything.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//TODO: DATABASE AND CONNECT TO BUTTON VIEW MODEL
public class SqliteDB extends SQLiteOpenHelper {

    public static final String TAG = "DATABASE";
    public static final String DB_NAME = "SmartingAnything.db";
    public static final int DB_VERSION = 1;

    //Button Table
    public static final String TABLE_BUTTONS = "buttons";
    public static final String B_ID = "bId";
    public static final String BUTTON_NAME = "button_name";
    public static final String BUTTON_STATE = "button_state";
    public static final String BUTTON_BACKGROUND_COLOR = "button_background_color";
    public static final String ACTION_ID = "action_id";

    //Action Table
    public static final String TABLE_ACTIONS = "actions";
    public static final String A_ID = "aId";
    public static final String ACTION_NAME = "action_name";
    public static final String ACTION_ON_MESSAGE = "action_on_message";
    public static final String ACTION_OFF_MESSAGE = "action_off_message";
    public static final String ACTION_R_MESSAGE = "action_r_message";


    public SqliteDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createActionTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_ACTIONS + " (" + A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " + ACTION_ON_MESSAGE + " TEXT , " + ACTION_NAME + " TEXT NOT NULL , " + ACTION_OFF_MESSAGE + " TEXT , " + ACTION_R_MESSAGE + " TEXT);";

        String createButtonTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_BUTTONS + " ( " + B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " + BUTTON_NAME + " TEXT NOT NULL , " + BUTTON_STATE + " TEXT NOT NULL , " + BUTTON_BACKGROUND_COLOR + " INTEGER NOT NULL , " + ACTION_ID + " INTEGER , " + "FOREIGN KEY (" + ACTION_ID + ") REFERENCES " + TABLE_ACTIONS + "(" + A_ID + "));";

        db.execSQL(createActionTableQuery);
        db.execSQL(createButtonTableQuery);
        Log.d(TAG, "Tables Created!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropActionTableQuery = "DROP TABLE IF EXISTS " + TABLE_ACTIONS;
        String dropButtonTableQuery = "DROP TABLE IF EXISTS " + TABLE_BUTTONS;
        db.execSQL(dropButtonTableQuery);
        db.execSQL(dropActionTableQuery);

        onCreate(db);
        Log.d(TAG, "Tables Updated!");
    }
}
