package com.watcher.car;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import static android.location.LocationManager.GPS_PROVIDER;

public class WatchingService extends IntentService {

    private LocationManager locationManager;

    public WatchingService() {
        super(WatchingService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationWatcher();
        locationManager.requestLocationUpdates(GPS_PROVIDER, 5000, 10, locationListener);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location lastKnownLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
        Log.i(WatchingService.class.getSimpleName(), "Lat: " + lastKnownLocation.getLatitude() + ", Lon: " + lastKnownLocation.getLongitude());
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
