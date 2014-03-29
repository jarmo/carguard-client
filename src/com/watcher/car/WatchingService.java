package com.watcher.car;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.watcher.car.Database.Item.*;

public class WatchingService extends IntentService {

  public static final int LOCATION_UPDATES_INTERVAL_MILLIS = 5 * 60 * 1000;
  public static final int LOCATION_UPDATES_MINIMUM_DISTANCE_METRES = 10;
  private SQLiteDatabase database;
  private LocationManager locationManager;
  public static final int BLUETOOTH_CONNECTION_TIMEOUT_MILLIS = 15 * 60 * 1000;
  public static Date latestBluetoothConnectionTime = new Date(new Date().getTime() - BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);

  private BroadcastReceiver bluetoothStatusHandler;

  public WatchingService() {
    super(WatchingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initialize();
  }

  protected void initialize() {
    database = new Database(this).getWritableDatabase();

    initializeLocationListener();

    if (isBluetoothConnectionTimedOut()) {
      initializeBluetoothListener();
    }
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(bluetoothStatusHandler);
    super.onDestroy();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
    if (location != null && latestBluetoothConnectionTime.getTime() <= new Date().getTime() - BLUETOOTH_CONNECTION_TIMEOUT_MILLIS) {
      try {
        sendLocationToServer(location);
      } catch (Exception e) {
        storeLocationLocally(location);
      }
    }

    sendPreviousLocationsToServer();

    TaskRunner.completeWakefulIntent(intent);
  }

  protected boolean isBluetoothConnectionTimedOut() {
    return latestBluetoothConnectionTime == null ||
      latestBluetoothConnectionTime.getTime() <= new Date().getTime() - BLUETOOTH_CONNECTION_TIMEOUT_MILLIS;
  }

  protected void initializeBluetoothListener() {
    bluetoothStatusHandler = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (ACTION_ACL_CONNECTED.equals(intent.getAction())) {
          Log.v(WatchingService.class.getSimpleName(), "Bluetooth connected");
          latestBluetoothConnectionTime = new Date();
        }
      }
    };

    registerReceiver(bluetoothStatusHandler, new IntentFilter(ACTION_ACL_CONNECTED));

    //noinspection ConstantConditions
    for (BluetoothDevice bluetoothDevice : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
      bluetoothDevice.fetchUuidsWithSdp();
    }
  }

  protected void initializeLocationListener() {
    locationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
    locationManager.requestLocationUpdates(
      GPS_PROVIDER, LOCATION_UPDATES_INTERVAL_MILLIS, LOCATION_UPDATES_MINIMUM_DISTANCE_METRES,
      new LocationListener() {
        @Override public void onLocationChanged(Location location) {}
        @Override public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override public void onProviderEnabled(String s) {}
        @Override public void onProviderDisabled(String s) {}
      });
  }

  private void sendLocationToServer(final Location location) {
    Map<String, Object> data = new HashMap<String, Object>() {{
      put("latitude", location.getLatitude());
      put("longitude", location.getLongitude());
      put("speed", location.getSpeed());
      put("time", location.getTime());
    }};

    try {
      new HttpClient().post(new JSONObject(data).toString());
    } catch (Exception e) {
      Log.e(WatchingService.class.getSimpleName(), "Failed to send location", e);
      throw new RuntimeException(e);
    }
  }

  private void storeLocationLocally(Location location) {
    ContentValues values = new ContentValues();
    values.put(LATITUDE, location.getLatitude());
    values.put(LONGITUDE, location.getLongitude());
    values.put(SPEED, location.getSpeed());
    values.put(CREATED_AT, location.getTime());

    database.insert(TABLE_NAME, "null", values);
  }

  private void sendPreviousLocationsToServer() {
    String[] projection = {
      _ID,
      LATITUDE,
      LONGITUDE,
      SPEED,
      CREATED_AT
    };

    String sortOrder = CREATED_AT + " DESC";

    Cursor results = database.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
    while (results.moveToNext()) {
      Location location = new Location(GPS_PROVIDER);
      location.setLatitude(results.getDouble(results.getColumnIndex(LATITUDE)));
      location.setLongitude(results.getDouble(results.getColumnIndex(LONGITUDE)));
      location.setSpeed(results.getFloat(results.getColumnIndex(SPEED)));
      location.setTime(results.getLong(results.getColumnIndex(CREATED_AT)));

      try {
        sendLocationToServer(location);
        database.delete(TABLE_NAME, _ID + "=?", new String[]{results.getString(results.getColumnIndex(_ID))});
      } catch (Exception e) {
        Log.e(WatchingService.class.getSimpleName(), "Failed to send previous location", e);
      }
    }
  }
}
