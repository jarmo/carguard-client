package com.watcher.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootHandler extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    new TaskRunner().schedule(context);
  }
}
