package com.javatomic.drupal.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Network utilities.
 */
public final class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * This class cannot be instantiated.
     */
    private NetworkUtils() {}

    /**
     * Retrieves the activate network information.
     *
     * @param context The context the caller is running within.
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo();
    }

    /**
     * Retrieves whether network connectivity exists and it is possible to establish connections
     * and pass data.
     *
     * @param context The context the caller is running within.
     * @return Returns true of the network if available, false otherwise.
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo netInfo = getActiveNetworkInfo(context);

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }

        return false;
    }
}
