package com.watcher.car;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

  private AlarmManager alarmManager;
  private PendingIntent alarmIntent;

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent service = new Intent(context, WatchingService.class);
    startWakefulService(context, service);
  }

  public void enableTask(Context context) {
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, AlarmReceiver.class);
    alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, 1000 * 60, alarmIntent);

    ComponentName receiver = new ComponentName(context, BootReceiver.class);
    PackageManager packageManager = context.getPackageManager();

    packageManager.setComponentEnabledSetting(receiver,
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      PackageManager.DONT_KILL_APP);
  }

  public void disableTask(Context context) {
    if (alarmManager != null) {
      alarmManager.cancel(alarmIntent);
    }

    // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
    // alarm when the device is rebooted.
    ComponentName receiver = new ComponentName(context, BootReceiver.class);
    PackageManager pm = context.getPackageManager();

    pm.setComponentEnabledSetting(receiver,
      PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
      PackageManager.DONT_KILL_APP);
  }
}
