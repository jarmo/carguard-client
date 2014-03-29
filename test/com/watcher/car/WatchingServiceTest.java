package com.watcher.car;

import android.location.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class WatchingServiceTest {
  @Before
  public void clearBlueToothTimeout() {
    WatchingService.latestBluetoothConnectionTime = null;
  }

  @Test
  public void initializesLocationListener() {
    WatchingService service = getService();
    service.initialize();

    verify(service).initializeLocationListener();
  }

  @Test
  public void initializeBluetoothListenerWhenConnectionHasTimedOut() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    service.initialize();

    verify(service).initializeBluetoothListener();
  }

  @Test
  public void doesNotInitializeBluetoothListenerWhenConnectionHasNotTimedOut() {
    WatchingService service = getService();
    doReturn(false).when(service).isBluetoothConnectionTimedOut();
    service.initialize();

    verify(service, never()).initializeBluetoothListener();
  }

  @Test
  public void isBluetoothConnectionTimedOutReturnsTrueWhenBluetoothConnectionHasNotBeenMade() {
    WatchingService.latestBluetoothConnectionTime = null;

    WatchingService service = getService();
    assertTrue(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void isBluetoothConnectionTimedOutReturnsTrueWhenBluetoothConnectionHasTimedOut() {
    WatchingService.latestBluetoothConnectionTime = new Date(new Date().getTime() - WatchingService.BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);

    WatchingService service = getService();
    assertTrue(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void isBluetoothConnectionTimedOutReturnsFalseWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService.latestBluetoothConnectionTime = new Date(new Date().getTime() - WatchingService.BLUETOOTH_CONNECTION_TIMEOUT_MILLIS + 1000);

    WatchingService service = getService();
    assertFalse(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void handleLocationEventIgnoresWhenNoLocation() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    service.handleLocationEvent(null);

    verify(service, never()).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventDoesNotSendLocationToServerWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService service = getService();
    doReturn(false).when(service).isBluetoothConnectionTimedOut();
    service.handleLocationEvent(new Location(GPS_PROVIDER));

    verify(service, never()).sendLocationToServer(any(Location.class));
    verify(service, never()).storeLocationLocally(any(Location.class));
  }

  @Test
  public void handleLocationEventSendsLocationToServerWhenBluetoothConnectionTimedOut() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));
    Location location = new Location(GPS_PROVIDER);
    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
    verify(service, never()).storeLocationLocally(location);
  }

  @Test
  public void handleLocationEventStoresLocationLocallyFailedToSendToServer() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doThrow(RuntimeException.class).when(service).sendLocationToServer(any(Location.class));
    doNothing().when(service).storeLocationLocally(any(Location.class));
    Location location = new Location(GPS_PROVIDER);
    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
    verify(service).storeLocationLocally(location);
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}
