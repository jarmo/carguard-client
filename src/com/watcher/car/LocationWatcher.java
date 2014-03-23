package com.watcher.car;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.watcher.car.Database.Item.*;

public class LocationWatcher implements LocationListener {
  private final SQLiteDatabase database;
  private SimpleDateFormat sqliteDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

  public LocationWatcher(SQLiteDatabase database) {
    this.database = database;
  }

  @Override
  public void onLocationChanged(Location location) {
    ContentValues values = new ContentValues();
    values.put(LATITUDE, location.getLatitude());
    values.put(LONGITUDE, location.getLongitude());
    values.put(SPEED, location.getSpeed());
    values.put(CREATED_AT, sqliteDateFormatter.format(new Date()));

    this.database.insert(TABLE_NAME, "null", values);
  }

  @Override
  public void onStatusChanged(String s, int i, Bundle bundle) {
  }

  @Override
  public void onProviderEnabled(String s) {
  }

  @Override
  public void onProviderDisabled(String s) {
  }
}
