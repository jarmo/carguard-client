package com.watcher.car;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.util.Log;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.watcher.car.Database.Item.*;

public class WatchingService extends IntentService {

  private SQLiteDatabase database;

  public WatchingService() {
    super(WatchingService.class.getSimpleName());

  }

  @Override
  public void onCreate() {
    super.onCreate();

    this.database = new Database(this).getWritableDatabase();

    ((LocationManager) getSystemService(LOCATION_SERVICE))
      .requestLocationUpdates(GPS_PROVIDER, 5000, 10, new LocationWatcher(this.database));
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String[] projection = {
      _ID,
      LATITUDE,
      LONGITUDE,
      SPEED,
      CREATED_AT
    };

    String sortOrder = CREATED_AT + " DESC";

    Cursor results = this.database.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
    while (results.moveToNext()) {
      final String latitude = results.getString(results.getColumnIndex(LATITUDE));
      final String longitude = results.getString(results.getColumnIndex(LONGITUDE));
      final String speed = results.getString(results.getColumnIndex(SPEED));
      final String createdAt = results.getString(results.getColumnIndex(CREATED_AT));

      Log.i(WatchingService.class.getSimpleName(), "Lat: " + latitude + ", Lon: " + longitude + ", " + "Speed: " + speed);
      Map<String, String> data = new HashMap<String, String>() {{
        put("latitude", latitude);
        put("longitude", longitude);
        put("speed", speed);
        put("time", createdAt);
      }};

      try {
        new HttpClient().post(new JSONObject(data).toString());
        this.database.delete(TABLE_NAME, _ID + "=?", new String[]{results.getString(results.getColumnIndex(_ID))});
      } catch (Exception e) {
        Log.e(WatchingService.class.getSimpleName(), "Failed to send location", e);
      }
    }

    AlarmReceiver.completeWakefulIntent(intent);
  }
}
