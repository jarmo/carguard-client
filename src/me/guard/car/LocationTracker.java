package me.guard.car;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static java.util.Calendar.HOUR_OF_DAY;
import static me.guard.car.Preferences.API_KEY_NAME;
import static me.guard.car.Preferences.SECRET_NAME;

public class LocationTracker implements Serializable {
  private static final long serialVersionUID = 0L;

  private static final int STORED_LOCATIONS_COUNT = 3;
  private static final int LOCATION_UPDATES_INTERVAL_IN_MILLIS = 60 * 1000;
  public static final int LOCATION_UPDATES_MINIMUM_DISTANCE_IN_METRES = 100;

  private transient Context context;
  private transient LocationManager locationManager;

  LimitedQueue<SerializableLocation> recentLocations = new LimitedQueue<SerializableLocation>(STORED_LOCATIONS_COUNT);
  LastMovingLocation lastMovingLocation;
  transient BatteryLevelManager batteryLevelManager = new BatteryLevelManager();
  public boolean hasSentLowBatteryAlert;

  public void setContext(Context context) {
    this.context = context;
    batteryLevelManager.setContext(context);
    locationManager = ((LocationManager) context.getSystemService(LOCATION_SERVICE));
  }

  public void startListener() {
    locationManager.requestLocationUpdates(
      GPS_PROVIDER,
      LOCATION_UPDATES_INTERVAL_IN_MILLIS,
      LOCATION_UPDATES_MINIMUM_DISTANCE_IN_METRES,
      new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
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
    );
  }

  public void stopListener() {
//    if (locationManager != null && locationListener != null) {
//      locationManager.removeUpdates(locationListener);
//      locationListener = null;
//    }
  }

  public boolean shouldSendHeartbeatLocation() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(HOUR_OF_DAY, -6);

    return lastMovingLocation == null || lastMovingLocation.date.before(calendar.getTime());
  }

  void sendLastLocationToServer(Map<String, Object> debugInformation) {
    SerializableLocation lastLocation = getLastKnownLocation();
    if (lastLocation == null) return;

    try {
      sendLocationToServer(lastLocation, debugInformation);
      Log.d(getClass().getSimpleName(), "Location sent to the server");
    } catch (Exception e) {
      Log.d(getClass().getSimpleName(), "Failed to send location to the server", e);
      storeLocationLocally(lastLocation);
    }

    lastMovingLocation = new LastMovingLocation(lastLocation);
  }

  public void sendPreviousLocationsToServer() {
    Database database = getDatabase();
    try {
      for (Map.Entry<String, JSONObject> locationWithId : database.getStoredLocationsData().entrySet()) {
        try {
          sendLocationDataToServer(locationWithId.getValue());
          database.delete(locationWithId.getKey());
        } catch (Exception e) {
          Log.e(getClass().getSimpleName(), "Failed to send previous location", e);
        }
      }
    } finally {
      database.close();
    }
  }

  private SerializableLocation getLastKnownLocation() {
    Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
    return location != null ? new SerializableLocation(location) : null;
  }

  boolean isMoving() {
    SerializableLocation lastKnownLocation = getLastKnownLocation();
    if (lastKnownLocation == null) return false;

    recentLocations.add(lastKnownLocation);

    if (lastMovingLocation == null) return true;
    if (recentLocations.size() != STORED_LOCATIONS_COUNT) return false;

    //for (SerializableLocation recentLocation : recentLocations) {
      //if (lastMovingLocation.location.getLocation().distanceTo(recentLocation.getLocation()) < LOCATION_UPDATES_MINIMUM_DISTANCE_IN_METRES)
        //return false;
    //}

    float speedInKmPerHour = lastKnownLocation.getLocation().getSpeed() / 1000 * 3600;
    return speedInKmPerHour >= 20;
  }

  void sendLocationToServer(SerializableLocation location, Map<String, Object> debugInformation) {
    Map<String, Object> data = getData(location);
    data.put("debug", debugInformation);
    sendLocationDataToServer(new JSONObject(data));
  }

  void storeLocationLocally(SerializableLocation location) {
    Database database = getDatabase();
    try {
      database.save(new JSONObject(getData(location)));
    } finally {
      database.close();
    }
  }

  private Map<String, Object> getData(SerializableLocation location) {
    Map<String, Object> data = location.getData();
    data.put("battery", batteryLevelManager.getBatteryLevel());

    if (lastMovingLocation != null) {
      data.put("distance", location.getLocation().distanceTo(lastMovingLocation.location.getLocation()));
    }

    return data;
  }

  private void sendLocationDataToServer(JSONObject data) {
    Preferences preferences = new Preferences(context);
    try {
      JSONObject json = new EncryptedJSONObject(data, preferences.get(SECRET_NAME));

      if (batteryLevelManager.isLowBattery()) {
        json = new JSONObject(json.toString());
        json.put("lowBattery", true);
        hasSentLowBatteryAlert = true;
      } else {
        hasSentLowBatteryAlert = false;
      }

      new HttpClient("/map/" + preferences.get(API_KEY_NAME)).post(json.toString());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private Database getDatabase() {
    return new Database(context);
  }

}
