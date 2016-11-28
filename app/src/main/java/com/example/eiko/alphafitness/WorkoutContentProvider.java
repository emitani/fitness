package com.example.eiko.alphafitness;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class WorkoutContentProvider extends ContentProvider {


    private final static String TAG = WorkoutContentProvider.class.getSimpleName();

    static final String PROVIDER = "com.example.eiko.alphafitness.workoutprovider";
    static final String URL = "content://" + PROVIDER + "/wosession";
    static final Uri URI = Uri.parse(URL);


    private Context mContext;
    private SQLiteDatabase db;

    private static HashMap<String, String> SESSION_PROJECTION_MAP;

    static final String DATABASE_NAME = "workout_db";
    static final String WORKOUT_SESSION_TABLE = "workout_sessions";
    static final int DATABASE_VERSION = 5;

    static final String DISTANCE = "distance";
    static final String DURATION = "duration";
    static final String CALORIES = "calories";
    static final String START_TIME = "start_time";
    static final String STEPS = "steps";

    static final String CREATE_DB_TABLE = "CREATE TABLE " + WORKOUT_SESSION_TABLE +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DISTANCE + " REAL NOT NULL," +
            DURATION + " INTEGER NOT NULL, " +
            CALORIES + " INTEGER NOT NULL," +
            STEPS + " INTEGER NOT NULL," +
            START_TIME + " INTEGER NOT NULL);";


    public WorkoutContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = db.insert(WORKOUT_SESSION_TABLE, "", values);
        return uri;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        if(mContext == null) {
            Log.e(TAG, "Failed to retrieve the context!");
        }

        DB dbHelper = new DB(mContext);
        db = dbHelper.getWritableDatabase();
        if(db == null) {
            Log.e(TAG, "Failed to create a writable database");
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(WORKOUT_SESSION_TABLE);

      //  queryBuilder.setProjectionMap(SESSION_PROJECTION_MAP);


        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    /**
     * DB helper inner class.
     */
    private static class DB extends SQLiteOpenHelper
    {
        DB(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + WORKOUT_SESSION_TABLE);
            onCreate(db);
        }
    }
}
