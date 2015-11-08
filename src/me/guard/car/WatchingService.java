package me.guard.car;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;

public class WatchingService extends IntentService {
  LocationTracker locationTracker;
  BluetoothConnectionManager bluetoothConnectionManager;
  BatteryLevelManager batteryLevelManager;

  public WatchingService() {
    super(WatchingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initialize();
  }

  protected void initialize() {
    locationTracker = new LocationTracker();
    bluetoothConnectionManager = new BluetoothConnectionManager();
    batteryLevelManager = new BatteryLevelManager();
    loadStateFromFile();

    bluetoothConnectionManager.setContext(this);
    batteryLevelManager.setContext(this);
    locationTracker.setContext(this);
  }

  @Override
  public void onDestroy() {
    saveStateToFile();

    super.onDestroy();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (bluetoothConnectionManager.isConnectionTimedOut()) {
      bluetoothConnectionManager.tryToEstablishConnection();
    }

    locationTracker.startListener();
    handleLocationEvent();
    completeWakefulIntent(intent);
  }

  protected void handleLocationEvent() {
    boolean isLowBattery = batteryLevelManager.isLowBattery();
    boolean hasSentLowBatteryAlert = locationTracker.hasSentLowBatteryAlert;
    boolean shouldSendHeartbeatLocation = locationTracker.shouldSendHeartbeatLocation();
    boolean isBluetoothConnectionTimedOut = bluetoothConnectionManager.isConnectionTimedOut();
    boolean isMoving = locationTracker.isMoving();

    if (isLowBattery && !hasSentLowBatteryAlert || !isLowBattery && hasSentLowBatteryAlert || shouldSendHeartbeatLocation || isBluetoothConnectionTimedOut && isMoving) {
      Map<String, Object> debugInformation = new HashMap<String, Object>();
      debugInformation.put("isLowBattery", isLowBattery);
      debugInformation.put("!hasSentLowBatteryAlert", !hasSentLowBatteryAlert);
      debugInformation.put("isLowBattery && !hasSentLowBatteryAlert", isLowBattery && !hasSentLowBatteryAlert);
      debugInformation.put("!isLowBattery && hasSentLowBatteryAlert", !isLowBattery && hasSentLowBatteryAlert);
      debugInformation.put("shouldSendHeartbeatLocation", shouldSendHeartbeatLocation);
      debugInformation.put("isBluetoothConnectionTimedOut", isBluetoothConnectionTimedOut);
      debugInformation.put("isMoving", isMoving);
      debugInformation.put("isBluetoothConnectionTimedOut && isMoving", isBluetoothConnectionTimedOut && isMoving);
      debugInformation.put("locationTracker.lastMovingLocation != null", locationTracker.lastMovingLocation != null);
      locationTracker.sendLastLocationToServer(debugInformation);
    }

    locationTracker.sendPreviousLocationsToServer();
  }

  private void loadStateFromFile() {
    try {
      Object object = Marshal.load(openFileInput("carguard-watching-service-location-tracker.ser"));
      if (object != null) {
        LocationTracker locationTrackerFromFile = (LocationTracker) object;
        locationTracker.recentLocations = locationTrackerFromFile.recentLocations;
        locationTracker.hasSentLowBatteryAlert = locationTrackerFromFile.hasSentLowBatteryAlert;
        locationTracker.lastMovingLocation = locationTrackerFromFile.lastMovingLocation;
      }
    } catch (FileNotFoundException ignored) {
    }

    try {
      Object object = Marshal.load(openFileInput("carguard-watching-service-bluetooth-connection-manager.ser"));
      if (object != null) {
        BluetoothConnectionManager bluetoothConnectionManagerFromFile = (BluetoothConnectionManager) object;
        bluetoothConnectionManager.latestConnectionTime = bluetoothConnectionManagerFromFile.latestConnectionTime;
      }
    } catch (FileNotFoundException ignored) {
    }
  }

  private void saveStateToFile() {
    try {
      Marshal.dump(openFileOutput("carguard-watching-service-location-tracker.ser", Context.MODE_PRIVATE), locationTracker);
    } catch (FileNotFoundException ignored) {
    }

    try {
      Marshal.dump(openFileOutput("carguard-watching-service-bluetooth-connection-manager.ser", Context.MODE_PRIVATE), bluetoothConnectionManager);
    } catch (FileNotFoundException ignored) {
    }
  }
}
