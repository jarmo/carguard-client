package me.guard.car;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_SCALE;

public class BatteryLevelManager {
  private Context context;

  public void setContext(Context context) {
    this.context = context;
  }

  public boolean isLowBattery() {
    return getBatteryLevel() <= 0.15f;
  }

  public float getBatteryLevel() {
    Intent batteryStatus = context.registerReceiver(null, new IntentFilter(ACTION_BATTERY_CHANGED));
    int level = batteryStatus.getIntExtra(EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(EXTRA_SCALE, -1);
    return level / (float) scale;
  }
}
