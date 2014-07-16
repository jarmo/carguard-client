package me.guard.car;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;

public class WatchingService extends IntentService {
  public static final int LOCATION_UPDATES_INTERVAL_MILLIS = 60 * 1000;
  public static final int LOCATION_UPDATES_MINIMUM_DISTANCE_METRES = 100;
  public static final int BLUETOOTH_CONNECTION_TIMEOUT_MILLIS = 15 * 60 * 1000;
  public static final int HEARTBEAT_TIMEOUT_MILLIS = 12 * 60 * 60 * 1000;

  static LocationTracker locationTracker = new LocationTracker();
  public static Date latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);

  private LocationManager locationManager;
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
    locationTracker.setContext(this);
    initializeLocationListener();

    if (shouldEstablishBluetoothConnection()) {
      initializeBluetoothListener();
    }
  }

  @Override
  public void onDestroy() {
    if (bluetoothStatusHandler != null) {
      unregisterReceiver(bluetoothStatusHandler);
    }
    super.onDestroy();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    handleLocationEvent(locationManager.getLastKnownLocation(GPS_PROVIDER));
    completeWakefulIntent(intent);
  }

  protected void handleLocationEvent(Location location) {
    Log.d(WatchingService.class.getSimpleName(), "Handling location event");
    //boolean shouldSendHeartBeatLocation = lastSentTime.getTime() <= timeoutTime(HEARTBEAT_TIMEOUT_MILLIS).getTime();
    //boolean isProbablyMoving = lastSentLocation != null && lastSentLocation.distanceTo(location) >= 100 || location.getSpeed() >= 10;
    locationTracker.sendLocationToServerWhenMoving(location);
    locationTracker.sendPreviousLocationsToServer();

//      if (shouldSendHeartBeatLocation || isProbablyMoving && isBluetoothConnectionTimedOut()) {
//        try {
//          sendLocationToServer(location);
//          Log.d(WatchingService.class.getSimpleName(), "Location sent to the server");
//        } catch (Exception e) {
//          Log.d(WatchingService.class.getSimpleName(), "Failed to send location to the server", e);
//          storeLocationLocally(location);
//        } finally {
//          lastSentTime = new Date();
//          lastSentLocation = location;
//        }
//      }
  }

  protected boolean shouldEstablishBluetoothConnection() {
    return latestBluetoothConnectionTime.getTime() <= timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS - LOCATION_UPDATES_INTERVAL_MILLIS).getTime();
  }

  protected boolean isBluetoothConnectionTimedOut() {
    return latestBluetoothConnectionTime.getTime() <= timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS).getTime();
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

  protected static Date timeoutTime(int timeoutMillis) {
    return new Date(new Date().getTime() - timeoutMillis);
  }
}
