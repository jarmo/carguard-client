package com.watcher.car;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class WatchingServiceTest {
  @Test
  public void initializesLocationListener() {
    WatchingService service = getService();
    service.initialize();

    verify(service).initializeLocationListener();
  }

  @Test
  public void initializeBluetoothListener() {
    WatchingService service = getService();
    service.initialize();

    verify(service).initializeBluetoothListener();
  }

  private WatchingService getService() {
    WatchingService service = spy(new WatchingService());
    doNothing().when(service).initializeLocationListener();
    return service;
  }
}
