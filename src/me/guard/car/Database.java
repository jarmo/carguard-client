package me.guard.car;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static me.guard.car.Database.Item.CREATED_AT;
import static me.guard.car.Database.Item.DATA;
import static me.guard.car.Database.Item.TABLE_NAME;
import static me.guard.car.Database.Item._ID;

public class Database extends SQLiteOpenHelper {
  public static final int DATABASE_VERSION = 3;
  public static final String DATABASE_NAME = "CarGuard.db";
  private final SQLiteDatabase sqlite;

  public Database(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    sqlite = getWritableDatabase();
  }

  public void save(JSONObject data) {
    ContentValues values = new ContentValues();
    values.put(DATA, data.toString());
    values.put(CREATED_AT, new Date().getTime());

    sqlite.insert(TABLE_NAME, "null", values);
  }

  public Map<String, JSONObject> getStoredLocationsData() {
    Map<String, JSONObject> locationsById = new HashMap<String, JSONObject>();
    Cursor cursor = sqlite.query(TABLE_NAME, new String[]{_ID, DATA, CREATED_AT}, null, null, null, null, CREATED_AT + " DESC");
    while (cursor.moveToNext()) {
      try {
        locationsById.put(cursor.getString(cursor.getColumnIndex(_ID)), new JSONObject(cursor.getString(cursor.getColumnIndex(DATA))));
      } catch (JSONException ignored) {
      }
    }
    cursor.close();

    return locationsById;
  }

  public void delete(String locationId) {
    sqlite.delete(TABLE_NAME, _ID + "=?", new String[]{locationId});
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table " + TABLE_NAME + "(" +
        _ID + " INTEGER PRIMARY KEY," +
        DATA + " TEXT," +
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
    public static final String DATA = "data";
    public static final String CREATED_AT = "created_at";
  }
}
