package com.watcher.car;

import android.location.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.watcher.car.WatchingService.HEARTBEAT_TIMEOUT_MILLIS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class WatchingServiceTest {
  @Before
  public void clearState() {
    WatchingService.latestBluetoothConnectionTime = null;
    WatchingService.lastKnownLocation = null;
    WatchingService.lastSavedLocation = null;
    WatchingService.lastSentTime = new Date();
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
  public void handleLocationEventIgnoresNearLocations() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();

    Location location1 = new Location(GPS_PROVIDER);
    location1.setLatitude(59.406270703834515);
    location1.setLongitude(24.69359735090019);
    WatchingService.lastKnownLocation = location1;

    Location location2 = new Location(GPS_PROVIDER);
    location2.setLatitude(59.406270703834515);
    location2.setLongitude(24.69359735090019);
    service.handleLocationEvent(location2);

    verify(service, never()).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventDoesNotIgnoreLocationsFurtherAway() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location1 = new Location(GPS_PROVIDER);
    location1.setLatitude(59.406270703834515);
    location1.setLongitude(24.69359735090019);
    WatchingService.lastKnownLocation = location1;

    Location location2 = new Location(GPS_PROVIDER);
    location2.setLatitude(59.406460703834515);
    location2.setLongitude(24.69539735090019);
    service.handleLocationEvent(location2);

    verify(service).sendLocationToServer(any(Location.class));
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

  @Test
  public void handleLocationEventDoesNotStoreLastSavedLocationLocallyFailedToSendToServer() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doThrow(RuntimeException.class).when(service).sendLocationToServer(any(Location.class));
    doNothing().when(service).storeLocationLocally(any(Location.class));
    Location location = spy(new Location(GPS_PROVIDER));
    doReturn(101f).when(location).distanceTo(any(Location.class));
    service.handleLocationEvent(location);
    service.handleLocationEvent(location);

    verify(service, times(2)).sendLocationToServer(location);
    verify(service).storeLocationLocally(location);
  }

  @Test
  public void handleLocationEventSendsLocationToServerWhenHeartbeatTimeoutHasBeenReached() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location = new Location(GPS_PROVIDER);
    location.setTime(new Date().getTime());
    WatchingService.lastKnownLocation = location;
    WatchingService.lastSentTime = new Date(new Date().getTime() - HEARTBEAT_TIMEOUT_MILLIS);

    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
  }

  @Test
  public void handleLocationEventDoesNotStoreLocationLocallyWhenSendingHeartbeatLocation() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doThrow(new RuntimeException()).when(service).sendLocationToServer(any(Location.class));
    doNothing().when(service).storeLocationLocally(any(Location.class));

    Location location = new Location(GPS_PROVIDER);
    location.setTime(new Date().getTime());
    WatchingService.lastKnownLocation = location;
    WatchingService.lastSentTime = new Date(new Date().getTime() - HEARTBEAT_TIMEOUT_MILLIS);

    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
    verify(service, never()).storeLocationLocally(any(Location.class));
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}
