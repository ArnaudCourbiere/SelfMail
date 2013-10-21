package com.javatomic.drupal.util;

import android.util.Log;

import com.javatomic.drupal.BuildConfig;

/**
 * Log utilities.
 */
public final class LogUtils {

    /**
     * This class cannot be instantiated
     */
    private LogUtils() {}

    public static void LOGD(String tag, String message) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(String tag, String message, Throwable cause) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);
        }
    }

    public static void LOGV(String tag, String message) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.v(tag, message);
        }
    }

    public static void LOGV(String tag, String message, Throwable cause) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.v(tag, message, cause);
        }
    }

    public static void LOGI(String tag, String message) {
        Log.i(tag, message);
    }

    public static void LOGI(String tag, String message, Throwable cause) {
        Log.i(tag, message, cause);
    }

    public static void LOGW(String tag, String message) {
        Log.w(tag, message);
    }

    public static void LOGW(String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void LOGE(String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }
}
