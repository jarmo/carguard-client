package me.guard.car;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static java.util.Calendar.MINUTE;

public class BluetoothConnectionManager implements Serializable {
  private static final long serialVersionUID = 0L;

  static final int CONNECTION_TIMEOUT_IN_MINUTES = 15;
  Date latestConnectionTime = new Date();
  private transient Context context;
  private BroadcastReceiver connectionStatusListener;

  public void setContext(Context context) {
    this.context = context;
  }

  public boolean isConnectionTimedOut() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(MINUTE, -CONNECTION_TIMEOUT_IN_MINUTES);

    return latestConnectionTime.before(calendar.getTime());
  }

  public void tryToEstablishConnection() {
    HandlerThread handlerThread = new HandlerThread("ht");
    handlerThread.start();

    connectionStatusListener = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (ACTION_ACL_CONNECTED.equals(intent.getAction())) {
          Log.v(getClass().getSimpleName(), "Bluetooth connected");
          latestConnectionTime = new Date();
        }
      }
    };
    context.registerReceiver(connectionStatusListener, new IntentFilter(ACTION_ACL_CONNECTED), null, new Handler(handlerThread.getLooper()));

    //noinspection ConstantConditions
    for (BluetoothDevice bluetoothDevice : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
      bluetoothDevice.fetchUuidsWithSdp();
    }

    waitForConnection();
  }

  public void stopListener() {
    if (context != null && connectionStatusListener != null) {
      context.unregisterReceiver(connectionStatusListener);
      connectionStatusListener = null;
    }
  }

  private void waitForConnection() {
    for (int i = 0; i < 20; i++) {
      if (!isConnectionTimedOut()) return;

      try {
        Thread.sleep(1000l);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
