package com.watcher.car;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;
import static android.content.Context.ALARM_SERVICE;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public class TaskRunner extends WakefulBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent service = new Intent(context, WatchingService.class);
    startWakefulService(context, service);
  }

  public void schedule(Context context) {
    AlarmManager scheduler = (AlarmManager) context.getSystemService(ALARM_SERVICE);

    scheduler.setInexactRepeating(
      ELAPSED_REALTIME_WAKEUP, 1000, 1000 * 60,
      PendingIntent.getBroadcast(context, 0, new Intent(context, TaskRunner.class), 0)
    );

    //noinspection ConstantConditions
    context.getPackageManager().setComponentEnabledSetting(
      new ComponentName(context, BootHandler.class),
      COMPONENT_ENABLED_STATE_ENABLED,
      DONT_KILL_APP
    );
  }
}