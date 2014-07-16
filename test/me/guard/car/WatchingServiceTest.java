package me.guard.car;

import android.location.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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
    WatchingService.locationTracker = new LocationTracker();
  }

  @Test
  public void initialize_initializesLocationListener() {
    WatchingService service = getService();
    service.initialize();

    verify(service).initializeLocationListener();
  }

  @Test
  public void initialize_setsLocationTrackerContext() {
    WatchingService service = getService();
    WatchingService.locationTracker = spy(new LocationTracker());
    service.initialize();

    verify(WatchingService.locationTracker).setContext(service);
  }

  @Test
  public void initialize_initializesBluetoothListenerWhenNeeded() {
    WatchingService service = getService();
    doReturn(true).when(service).shouldEstablishBluetoothConnection();
    service.initialize();

    verify(service).initializeBluetoothListener();
  }

  @Test
  public void initialize_doesNotInitializeBluetoothListenerWhenNotNeeded() {
    WatchingService service = getService();
    doReturn(false).when(service).shouldEstablishBluetoothConnection();
    service.initialize();

    verify(service, never()).initializeBluetoothListener();
  }

  @Test
  public void shouldEstablishBluetoothConnection_trueWhenTimeoutIsNear() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS - LOCATION_UPDATES_INTERVAL_MILLIS);

    WatchingService service = getService();
    assertTrue(service.shouldEstablishBluetoothConnection());
  }

  @Test
  public void shouldEstablishBluetoothConnection_falseWhenTimeoutIsNotExceeded() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS - 2 * LOCATION_UPDATES_INTERVAL_MILLIS);

    WatchingService service = getService();
    assertFalse(service.shouldEstablishBluetoothConnection());
  }

  @Test
  public void isBluetoothConnectionTimedOut_trueWhenBluetoothConnectionHasTimedOut() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS);

    WatchingService service = getService();
    assertTrue(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void isBluetoothConnectionTimedOut_falseWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService.latestBluetoothConnectionTime = timeoutTime(BLUETOOTH_CONNECTION_TIMEOUT_MILLIS - 1000);

    WatchingService service = getService();
    assertFalse(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void handleLocationEvent_sendsLocationToServerWhenMoving() {
    WatchingService service = getService();
    WatchingService.locationTracker = spy(new LocationTracker());
    doNothing().when(WatchingService.locationTracker).sendLocationToServerWhenMoving(any(Location.class));
    doNothing().when(WatchingService.locationTracker).sendPreviousLocationsToServer();

    Location location = new Location(GPS_PROVIDER);
    service.handleLocationEvent(location);

    verify(WatchingService.locationTracker).sendLocationToServerWhenMoving(location);
  }

  @Test
  public void handleLocationEvent_sendsPreviousLocationsToServer() {
    WatchingService service = getService();
    WatchingService.locationTracker = spy(new LocationTracker());
    doNothing().when(WatchingService.locationTracker).sendLocationToServerWhenMoving(any(Location.class));
    doNothing().when(WatchingService.locationTracker).sendPreviousLocationsToServer();

    Location location = new Location(GPS_PROVIDER);
    service.handleLocationEvent(location);

    verify(WatchingService.locationTracker).sendPreviousLocationsToServer();
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}
