package me.guard.car;

import java.io.Serializable;
import java.util.Date;

public class LastMovingLocation implements Serializable {
  private static final long serialVersionUID = 0L;

  public Date date = new Date();
  public SerializableLocation location;

  public LastMovingLocation(SerializableLocation location) {
    this.location = location;
  }
}