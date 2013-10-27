package com.javatomic.drupal.auth;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * TODO
 */
public abstract class Authenticator {
    private static final String TAG = "Authenticator";

    public static final int GET_AUTH_TOKEN_REQUEST = 2000;

    /**
     *
     * @return New instance of the authenticator.
     */
    public abstract Authenticator newInstance();

    /**
     * Retrieves and returns an authentication token for the specified account. If user interaction
     * is required, the {@link Authenticator} should invoke {@link Activity#startActivityForResult(android.content.Intent, int)}
     * using {@link #GET_AUTH_TOKEN_REQUEST} request code, so that the calling activity can catch
     * the result of the {@link Intent} and respond to it.
     *
     * @param caller Activity requesting the token (used in case user interaction is needed).
     * @param account Account to get the token for.
     * @return
     */
    public abstract String getToken(Activity caller, Account account);

    /**
     *
     * @param context
     * @param token
     */
    public abstract void invalidateToken(Context context, String token);
}
