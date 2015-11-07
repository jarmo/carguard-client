package me.guard.car;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;

public class WatchingService extends IntentService {
  static LocationTracker locationTracker = new LocationTracker();
  static BluetoothConnectionManager bluetoothConnectionManager = new BluetoothConnectionManager();
  static BatteryLevelManager batteryLevelManager = new BatteryLevelManager();

  public WatchingService() {
    super(WatchingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initialize();
  }

  protected void initialize() {
    Object object = deserializeObject("carguard-watching-service-location-tracker.ser");
    if (object != null) {
      LocationTracker deserializedLocationTracker = (LocationTracker) object;
      locationTracker.limitedQueue = deserializedLocationTracker.limitedQueue;
      locationTracker.hasSentLowBatteryAlert = deserializedLocationTracker.hasSentLowBatteryAlert;
      locationTracker.lastMovingLocation = deserializedLocationTracker.lastMovingLocation;
    }

    object = deserializeObject("carguard-watching-service-bluetooth-connection-manager.ser");
    if (object != null) {
      BluetoothConnectionManager deserializedBluetoothConnectionManager = (BluetoothConnectionManager) object;
      bluetoothConnectionManager.latestConnectionTime = deserializedBluetoothConnectionManager.latestConnectionTime;
    }

    bluetoothConnectionManager.setContext(this);
    if (bluetoothConnectionManager.isConnectionTimedOut()) {
      bluetoothConnectionManager.tryToEstablishConnection();
    }

    batteryLevelManager.setContext(this);
    locationTracker.setContext(this);
    locationTracker.startListener();
  }

  private Object deserializeObject(String fileName) {
    ObjectInputStream objectinputstream = null;
    FileInputStream streamIn = null;
    Object deserializedObject = null;
    try {
      streamIn = openFileInput(fileName);
      objectinputstream = new ObjectInputStream(streamIn);
      deserializedObject = objectinputstream.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (OptionalDataException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (StreamCorruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (objectinputstream != null) {
        try {
          objectinputstream.close();
          streamIn.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return deserializedObject;
  }

  @Override
  public void onDestroy() {
    serializeObject("carguard-watching-service-location-tracker.ser", locationTracker);
    serializeObject("carguard-watching-service-bluetooth-connection-manager.ser", bluetoothConnectionManager);

    bluetoothConnectionManager.stopSearching();

    super.onDestroy();
  }

  private void serializeObject(String fileName, Object object) {
    ObjectOutputStream oos = null;
    FileOutputStream fout = null;
    try {
      fout = openFileOutput(fileName, Context.MODE_PRIVATE);
      oos = new ObjectOutputStream(fout);
      oos.writeObject(object);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (oos != null) {
        try {
          oos.close();
          fout.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
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
}
