package com.watcher.car;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Date;

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
  public void isBluetoothConnectionTimedOutReturnsTrueWhenBluetoothConnectionHasAlmostTimedOut() {
    WatchingService.latestBluetoothConnectionTime = new Date(new Date().getTime() - WatchingService.BLUETOOTH_CONNECTION_TIMEOUT_MILLIS + 60 * 1000);

    WatchingService service = getService();
    assertTrue(service.isBluetoothConnectionTimedOut());
  }

  @Test
  public void isBluetoothConnectionTimedOutReturnsFalseWhenBluetoothConnectionHasNotTimedOut() {
    WatchingService.latestBluetoothConnectionTime = new Date(new Date().getTime() - WatchingService.BLUETOOTH_CONNECTION_TIMEOUT_MILLIS + 61 * 1000);

    WatchingService service = getService();
    assertFalse(service.isBluetoothConnectionTimedOut());
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}