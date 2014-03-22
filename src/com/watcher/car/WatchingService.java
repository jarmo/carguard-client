package com.watcher.car;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class WatchingService extends IntentService {
    public WatchingService() {
        super("WatchingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(WatchingService.class.getName(), "Yep");
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
