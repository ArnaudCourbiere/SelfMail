package com.javatomic.drupal.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * <p>
 * A {@link NetworkReceiver} is dynamic {@link BroadcastReceiver} used to listen for network state changes.
 * A thread can use this object by calling {@link #waitForNetwork()}.
 * If the network is already available, the thread will not wait and the call will return immediately.
 * If the network is not available, the thread will wait until the network is available.
 * When the network becomes available, the {@link com.javatomic.drupal.net.NetworkReceiver} will
 * wake up up any threads that is currently waiting.
 * </p>
 *
 * <p>
 * <em>
 * It is the responsibility of the caller to
 * register the {@link NetworkReceiver} using {@link Context#registerReceiver(BroadcastReceiver, IntentFilter)}
 * to receive {@link ConnectivityManager#CONNECTIVITY_ACTION} events and to unregister it when
 * the {@link NetworkReceiver} is not needed anymore.
 * </em>
 * </p>
 *
 */
public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";

    /**
     * Monitor used for thread synchronization.
     */
    private Object mLock;

    /**
     * The Context in which the receiver is running.
     */
    private Context mContext;

    /**
     * Creates a new NetworkReceiver.
     *
     * @param context The Context in which the receiver is running.
     */
     public NetworkReceiver(Context context) {
         mLock = new Object();
         mContext = context;
     }

    /**
     * If the network is available, wakes up any thread currently waiting for the network.
     *
     * @param context The Context in which the receiver is running
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtils.isNetworkConnected(context)) {
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    }

    /**
     * Waits until the network becomes available. If the network is already available, this method
     * returns immediately.
     *
     * @throws InterruptedException If another thread interrupts this thread while it is waiting.
     */
    public void waitForNetwork() throws InterruptedException {
        synchronized (mLock) {
            while(!NetworkUtils.isNetworkConnected(mContext)) {
                mLock.wait();
            }
        }
    }
}
