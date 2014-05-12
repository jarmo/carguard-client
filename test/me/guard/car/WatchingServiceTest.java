package me.guard.car;

import android.location.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

import static android.location.LocationManager.GPS_PROVIDER;
import static me.guard.car.WatchingService.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class WatchingServiceTest {
  @Before
  public void clearState() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);
    WatchingService.lastSentLocation = null;
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
  public void isBluetoothConnectionTimedOutReturnsTrueWhenBluetoothConnectionHasTimedOut() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);

    WatchingService service = getService();
    assertTrue(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void isBluetoothConnectionTimedOutReturnsFalseWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService.latestBluetoothConnectionTime = new Date(timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS).getTime() + 1000);

    WatchingService service = getService();
    assertFalse(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void handleLocationEventIgnoresWhenNoLocation() {
    WatchingService service = getService();
    service.handleLocationEvent(null);

    verify(service, never()).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventIgnoresNearLocations() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();

    Location location1 = spy(new Location(GPS_PROVIDER));
    WatchingService.lastSentLocation = location1;

    Location location2 = new Location(GPS_PROVIDER);
    doReturn(99f).when(location1).distanceTo(location2);
    service.handleLocationEvent(location2);

    verify(service, never()).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventDoesNotIgnoreLocationsFurtherAway() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location1 = spy(new Location(GPS_PROVIDER));
    WatchingService.lastSentLocation = location1;

    Location location2 = new Location(GPS_PROVIDER);
    doReturn(100f).when(location1).distanceTo(location2);
    service.handleLocationEvent(location2);

    verify(service).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventIgnoresLocationsWithSlowSpeeds() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location = spy(new Location(GPS_PROVIDER));
    doReturn(9f).when(location).getSpeed();
    service.handleLocationEvent(location);

    verify(service, never()).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventDoesNotIgnoreLocationsWithFasterSpeeds() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location1 = spy(new Location(GPS_PROVIDER));
    WatchingService.lastSentLocation = location1;

    Location location2 = spy(new Location(GPS_PROVIDER));
    doReturn(99f).when(location1).distanceTo(location2);
    doReturn(10f).when(location2).getSpeed();
    service.handleLocationEvent(location2);

    verify(service).sendLocationToServer(any(Location.class));
  }

  @Test
  public void handleLocationEventDoesNotSendLocationToServerWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService service = getService();
    doReturn(false).when(service).isBluetoothConnectionTimedOut();
    Location location = spy(new Location(GPS_PROVIDER));
    doReturn(10f).when(location).getSpeed();
    service.handleLocationEvent(location);

    verify(service, never()).sendLocationToServer(any(Location.class));
    verify(service, never()).storeLocationLocally(any(Location.class));
  }

  @Test
  public void handleLocationEventStoresLocationLocallyFailedToSendToServer() {
    WatchingService service = getService();
    doReturn(true).when(service).isBluetoothConnectionTimedOut();
    doThrow(RuntimeException.class).when(service).sendLocationToServer(any(Location.class));
    doNothing().when(service).storeLocationLocally(any(Location.class));
    Location location = spy(new Location(GPS_PROVIDER));
    doReturn(10f).when(location).getSpeed();
    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
    verify(service).storeLocationLocally(location);
  }

  @Test
  public void handleLocationEventSendsLocationToServerWhenHeartbeatTimeoutHasBeenReached() {
    WatchingService service = getService();
    doReturn(false).when(service).isBluetoothConnectionTimedOut();
    doNothing().when(service).sendLocationToServer(any(Location.class));

    Location location = new Location(GPS_PROVIDER);
    location.setTime(new Date().getTime());
    WatchingService.lastSentLocation = location;
    WatchingService.lastSentTime = timeoutTime(HEARTBEAT_TIMEOUT_MILLIS);

    service.handleLocationEvent(location);

    verify(service).sendLocationToServer(location);
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}
