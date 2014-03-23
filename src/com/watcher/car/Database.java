package com.watcher.car;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static com.watcher.car.Database.Item.*;

public class Database extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "CarWatcher.db";

  public Database(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_NAME + "(" +
        _ID + " INTEGER PRIMARY KEY," +
        LATITUDE + " REAL," +
        LONGITUDE + " REAL," +
        SPEED + " REAL," +
        CREATED_AT + " INTEGER)"
    );
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public static abstract class Item implements BaseColumns {
    public static final String TABLE_NAME = "location";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SPEED = "speed";
    public static final String CREATED_AT = "created_at";
  }
}
