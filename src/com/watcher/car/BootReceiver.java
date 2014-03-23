package com.watcher.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

public class BootReceiver extends BroadcastReceiver {
  AlarmReceiver alarm = new AlarmReceiver();

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      alarm.enableTask(context);
    }
  }
}
