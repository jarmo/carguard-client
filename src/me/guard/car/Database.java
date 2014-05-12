package me.guard.car;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;
import static me.guard.car.Database.Item.*;

public class Database extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 2;
  public static final String DATABASE_NAME = "CarGuard.db";
  private final SQLiteDatabase sqlite;

  public Database(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    sqlite = getWritableDatabase();
  }

  public void save(Location location) {
    ContentValues values = new ContentValues();
    values.put(LATITUDE, location.getLatitude());
    values.put(LONGITUDE, location.getLongitude());
    values.put(SPEED, location.getSpeed());
    values.put(CREATED_AT, location.getTime());

    sqlite.insert(TABLE_NAME, "null", values);
  }

  public Map<String, Location> getStoredLocations() {
    Map<String, Location> locations = new HashMap<String, Location>();
    Cursor cursor = sqlite.query(TABLE_NAME, new String[]{_ID, LATITUDE, LONGITUDE, SPEED, CREATED_AT}, null, null, null, null, CREATED_AT + " DESC");
    while (cursor.moveToNext()) {
      Location location = new Location(GPS_PROVIDER);
      location.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
      location.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
      location.setSpeed(cursor.getFloat(cursor.getColumnIndex(SPEED)));
      location.setTime(cursor.getLong(cursor.getColumnIndex(CREATED_AT)));

      locations.put(cursor.getString(cursor.getColumnIndex(_ID)), location);
    }

    return locations;
  }

  public void delete(String locationId) {
    sqlite.delete(TABLE_NAME, _ID + "=?", new String[]{locationId});
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_NAME + "(" +
        _ID + " INTEGER PRIMARY KEY," +
        LATITUDE + " REAL," +
        LONGITUDE + " REAL," +
        SPEED + " REAL," +
        CREATED_AT + " INTEGER)"
    );
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  @Override
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
