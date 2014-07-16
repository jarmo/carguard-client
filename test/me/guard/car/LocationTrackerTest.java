package me.guard.car;

import android.location.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static android.location.LocationManager.GPS_PROVIDER;
import static me.guard.car.LocationTracker.LastMovingLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class LocationTrackerTest {
  @Test
  public void sendLocationToServerWhenMoving_addsLocationIntoQueue() {
    TestLocation location = new TestLocation();
    LocationTracker locationTracker = createLocationTracker();
    locationTracker.sendLocationToServerWhenMoving(location);

    assertTrue(locationTracker.limitedQueue.contains(location));
  }

  @Test
  public void sendLocationToServerWhenMoving_ignoresWhenNoLocation() {
    LocationTracker locationTracker = spy(createLocationTracker());
    try {
      locationTracker.sendLocationToServerWhenMoving(null);
    } catch (Exception e) {
      fail("Should not have thrown any Exceptions");
    }

    assertTrue(locationTracker.limitedQueue.isEmpty());
    verify(locationTracker, never()).isMoving();
  }

  @Test
  public void sendLocationToServerWhenMoving_sendsLastLocationToServerWhenIsMoving() {
    LocationTracker locationTracker = spy(createLocationTracker());
    doReturn(true).when(locationTracker).isMoving();
    doNothing().when(locationTracker).sendLastLocationToServer();

    locationTracker.sendLocationToServerWhenMoving(new TestLocation());
    verify(locationTracker).sendLastLocationToServer();
  }

  @Test
  public void sendLocationToServerWhenMoving_doesNotSendLastLocationToServerWhenIsNotMoving() {
    LocationTracker locationTracker = spy(createLocationTracker());
    doReturn(false).when(locationTracker).isMoving();
    doNothing().when(locationTracker).sendLastLocationToServer();

    locationTracker.sendLocationToServerWhenMoving(new TestLocation());
    verify(locationTracker, never()).sendLastLocationToServer();
  }

  @Test
  public void isMoving_isFalseWhenQueueIsNotFull() {
    LocationTracker locationTracker = createLocationTracker();
    locationTracker.limitedQueue.add(new TestLocation());
    locationTracker.limitedQueue.add(new TestLocation());

    assertFalse(locationTracker.isMoving());
  }

  @Test
  public void isMoving_isTrueWhenQueueIsFullAndLastMovingLocationIsNotSet() {
    LocationTracker locationTracker = createLocationTracker();
    locationTracker.limitedQueue.add(new TestLocation());
    locationTracker.limitedQueue.add(new TestLocation());
    locationTracker.limitedQueue.add(new TestLocation());

    assertTrue(locationTracker.isMoving());
  }

  @Test
  public void isMoving_isTrueWhenQueueIsFullAndLocationsHaveDistanceBetweenLastMovingLocation() {
    LocationTracker locationTracker = createLocationTracker();
    TestLocation lastMovingLocation = spy(new TestLocation(1));
    locationTracker.lastMovingLocation = new LastMovingLocation(lastMovingLocation);
    doReturn(100f).when(lastMovingLocation).distanceTo(any(Location.class));
    TestLocation location = spy(new TestLocation());

    locationTracker.limitedQueue.add(location);
    locationTracker.limitedQueue.add(location);
    locationTracker.limitedQueue.add(location);

    assertTrue(locationTracker.isMoving());
    verify(lastMovingLocation, times(3)).distanceTo(location);
  }

  @Test
  public void isMoving_isFalseWhenQueueIsFullAndAtLeastOneLocationDoesNotHaveDistanceBetweenLastMovingLocation() {
    LocationTracker locationTracker = createLocationTracker();
    TestLocation lastMovingLocation = spy(new TestLocation(1));
    locationTracker.lastMovingLocation = new LastMovingLocation(lastMovingLocation);
    TestLocation locationWithDistance = spy(new TestLocation(2));
    doReturn(100f).when(lastMovingLocation).distanceTo(locationWithDistance);
    TestLocation locationWithoutDistance = spy(new TestLocation(3));
    doReturn(99f).when(lastMovingLocation).distanceTo(locationWithoutDistance);

    locationTracker.limitedQueue.add(locationWithDistance);
    locationTracker.limitedQueue.add(locationWithoutDistance);
    locationTracker.limitedQueue.add(locationWithDistance);

    assertFalse(locationTracker.isMoving());
    verify(lastMovingLocation, times(1)).distanceTo(locationWithDistance);
    verify(lastMovingLocation, times(1)).distanceTo(locationWithoutDistance);
  }

  @Test
  public void sendLastLocationToServer_sendsLatestLocationToServerAndStoresItIntoLastMovingLocation() {
    LocationTracker locationTracker = spy(createLocationTracker());
    locationTracker.limitedQueue.add(new TestLocation(1));
    TestLocation lastLocation = new TestLocation(2);
    locationTracker.limitedQueue.add(lastLocation);

    doNothing().when(locationTracker).sendLocationToServer(any(Location.class));
    locationTracker.sendLastLocationToServer();

    assertTrue(locationTracker.lastMovingLocation.location.equals(lastLocation));
    verify(locationTracker).sendLocationToServer(lastLocation);
  }

  @Test
  public void sendLastLocationToServer_sendsLatestLocationToServerAndStoresItIntoLastMovingLocationEvenIfSendFails() {
    LocationTracker locationTracker = spy(createLocationTracker());
    TestLocation lastLocation = new TestLocation(2);
    locationTracker.limitedQueue.add(lastLocation);

    doThrow(new RuntimeException()).when(locationTracker).sendLocationToServer(any(Location.class));
    doNothing().when(locationTracker).storeLocationLocally(any(Location.class));
    locationTracker.sendLastLocationToServer();

    assertTrue(locationTracker.lastMovingLocation.location.equals(lastLocation));
  }

  @Test
  public void sendLastLocationToServer_storesLocationLocallyWhenSendFails() {
    LocationTracker locationTracker = spy(createLocationTracker());
    TestLocation lastLocation = new TestLocation(2);
    locationTracker.limitedQueue.add(lastLocation);

    doThrow(new RuntimeException()).when(locationTracker).sendLocationToServer(any(Location.class));
    doNothing().when(locationTracker).storeLocationLocally(any(Location.class));
    locationTracker.sendLastLocationToServer();

    verify(locationTracker).storeLocationLocally(lastLocation);
  }

  private LocationTracker createLocationTracker() {
    return new LocationTracker();
  }

  class TestLocation extends Location {
    public TestLocation() {
      super(GPS_PROVIDER);
    }

    public TestLocation(long fixTime) {
      super(GPS_PROVIDER);
      setTime(fixTime);
    }
  }
}