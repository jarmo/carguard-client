package com.watcher.car;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.watcher.car.Database.Item.*;

public class WatchingService extends IntentService {

  private SQLiteDatabase database;
  private LocationManager locationManager;
  private static Date latestBluetoothConnection = new Date(new Date().getTime() - 5 * 60 * 1000);

  private BroadcastReceiver bluetoothStatusHandler = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (ACTION_ACL_CONNECTED.equals(intent.getAction())) {
        Log.e(WatchingService.class.getSimpleName(), "Connected");
        latestBluetoothConnection = new Date();
      }
    }
  };

  public WatchingService() {
    super(WatchingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();

    this.database = new Database(this).getWritableDatabase();

    locationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
    locationManager.requestLocationUpdates(GPS_PROVIDER, 10000, 10, new LocationWatcher());

    this.registerReceiver(bluetoothStatusHandler, new IntentFilter(ACTION_ACL_CONNECTED));

    //noinspection ConstantConditions
    for (BluetoothDevice bluetoothDevice : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
      bluetoothDevice.fetchUuidsWithSdp();
    }
  }

  @Override
  public void onDestroy() {
    this.unregisterReceiver(bluetoothStatusHandler);
    super.onDestroy();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
    if (location != null && latestBluetoothConnection.getTime() <= new Date().getTime() - 5 * 60 * 1000) {
      try {
        sendLocationToServer(location);
      } catch (Exception e) {
        storeLocationLocally(location);
      }
    }

    sendPreviousLocationsToServer();

    AlarmReceiver.completeWakefulIntent(intent);
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

    this.database.insert(TABLE_NAME, "null", values);
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

    Cursor results = this.database.query(TABLE_NAME, projection, null, null, null, null, sortOrder);
    while (results.moveToNext()) {
      Location location = new Location(GPS_PROVIDER);
      location.setLatitude(results.getDouble(results.getColumnIndex(LATITUDE)));
      location.setLongitude(results.getDouble(results.getColumnIndex(LONGITUDE)));
      location.setSpeed(results.getFloat(results.getColumnIndex(SPEED)));
      location.setTime(results.getLong(results.getColumnIndex(CREATED_AT)));

      try {
        sendLocationToServer(location);
        this.database.delete(TABLE_NAME, _ID + "=?", new String[]{results.getString(results.getColumnIndex(_ID))});
      } catch (Exception e) {
        Log.e(WatchingService.class.getSimpleName(), "Failed to send previous location", e);
      }
    }
  }
}
