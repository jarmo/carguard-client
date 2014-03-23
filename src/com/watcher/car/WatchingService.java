package com.watcher.car;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

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
      SPEED
    };

    String sortOrder = CREATED_AT + " DESC";

    Cursor results = this.database.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
    while (results.moveToNext()) {
      String latitude = results.getString(results.getColumnIndex(LATITUDE));
      String longitude = results.getString(results.getColumnIndex(LONGITUDE));
      String speed = results.getString(results.getColumnIndex(SPEED));
      Log.i(WatchingService.class.getSimpleName(), "Lat: " + latitude + ", Lon: " + longitude + ", " + "Speed: " + speed);
    }

    AlarmReceiver.completeWakefulIntent(intent);
  }
}
