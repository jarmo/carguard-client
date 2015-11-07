package me.guard.car;

import android.location.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableLocation implements Serializable {
  private static final long serialVersionUID = 0L;

  private transient Location location;
  private final String provider;
  private Map<String, Object> data = new HashMap<String, Object>();

  public SerializableLocation(Location location) {
    this.location = location;
    this.provider = location.getProvider();
    this.data = getData();
  }

  public Map<String, Object> getData() {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("latitude", location.getLatitude());
    data.put("longitude", location.getLongitude());
    data.put("speed", location.getSpeed());
    data.put("hasAccuracy", location.hasAccuracy());
    data.put("hasAccuracy", location.hasAccuracy());
    data.put("accuracy", location.getAccuracy());
    data.put("fixTime", location.getTime());

    return data;
  }

  public Location getLocation() {
    if (location != null) return location;

    Location locationFromData = new Location(provider);
    locationFromData.setLatitude((Double) data.get("latitude"));
    locationFromData.setLongitude((Double) data.get("longitude"));
    locationFromData.setSpeed((Float) data.get("speed"));
    locationFromData.setAccuracy((Float) data.get("accuracy"));
    locationFromData.setTime((Long) data.get("fixTime"));

    location = locationFromData;
    return location;
  }
}
