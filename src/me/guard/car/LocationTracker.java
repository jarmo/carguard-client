package me.guard.car;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_SCALE;
import static me.guard.car.Preferences.API_KEY_NAME;
import static me.guard.car.Preferences.SECRET_NAME;

public class LocationTracker {
  private static final int STORED_LOCATIONS_COUNT = 3;
  private Context context;

  final LimitedQueue<Location> limitedQueue = new LimitedQueue<Location>(STORED_LOCATIONS_COUNT);
  LastMovingLocation lastMovingLocation;

  public void setContext(Context context) {
    this.context = context;
  }

  public void sendLocationToServerWhenMoving(Location location) {
    if (location == null) return;

    limitedQueue.add(location);
    if (isMoving()) sendLastLocationToServer();
  }

  public void sendPreviousLocationsToServer() {
    Database database = getDatabase();
    for (Map.Entry<String, JSONObject> locationWithId : database.getStoredLocationsData().entrySet()) {
      try {
        sendLocationDataToServer(locationWithId.getValue());
        database.delete(locationWithId.getKey());
      } catch (Exception e) {
        Log.e(WatchingService.class.getSimpleName(), "Failed to send previous location", e);
      }
    }
  }

  boolean isMoving() {
    if (limitedQueue.size() == STORED_LOCATIONS_COUNT) {
      if (lastMovingLocation == null) {
        return true;
      } else {
        for (Location location : limitedQueue) {
          if (lastMovingLocation.location.distanceTo(location) < 100) return false;
        }

        return true;
      }
    }

    return false;
  }

  void sendLastLocationToServer() {
    Location lastLocation = limitedQueue.getLast();
    try {
      sendLocationToServer(lastLocation);
      Log.d(getClass().getSimpleName(), "Location sent to the server");
    } catch (Exception e) {
      Log.d(getClass().getSimpleName(), "Failed to send location to the server", e);
      storeLocationLocally(lastLocation);
    }

    lastMovingLocation = new LastMovingLocation(lastLocation);
  }

  void sendLocationToServer(Location location) {
    sendLocationDataToServer(new JSONObject(getData(location)));
  }

  private void sendLocationDataToServer(JSONObject data) {
    Preferences preferences = new Preferences(context);
    try {
      new HttpClient(preferences.get(API_KEY_NAME)).post(new EncryptedJSONObject(data, preferences.get(SECRET_NAME)).toString());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  void storeLocationLocally(Location location) {
    getDatabase().save(new JSONObject(getData(location)));
  }

  private Map<String, Object> getData(final Location location) {
    Intent batteryStatus = context.registerReceiver(null, new IntentFilter(ACTION_BATTERY_CHANGED));
    int level = batteryStatus.getIntExtra(EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(EXTRA_SCALE, -1);
    final float batteryPercentage = level / (float)scale;

    Map<String, Object> data = new HashMap<String, Object>() {{
      put("latitude", location.getLatitude());
      put("longitude", location.getLongitude());
      put("speed", location.getSpeed());
      put("battery", batteryPercentage);
      put("fixTime", location.getTime());
    }};

    if (lastMovingLocation != null) {
      data.put("distance", location.distanceTo(lastMovingLocation.location));
    }

    return data;
  }

  private Database getDatabase() {
    return new Database(context);
  }

  public static class LastMovingLocation {
    public final Date date;
    public final Location location;

    public LastMovingLocation(Location location) {
      this.location = location;
      this.date = new Date();
    }
  }
}
