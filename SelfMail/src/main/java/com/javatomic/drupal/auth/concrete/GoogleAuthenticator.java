package com.javatomic.drupal.auth.concrete;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.javatomic.drupal.auth.Authenticator;
import com.javatomic.drupal.auth.AuthenticatorFactory;

import java.io.IOException;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * TODO
 */
public class GoogleAuthenticator extends Authenticator {
    private static final String TAG = "GoogleAuthenticator";

    /*
     * Register authenticator type to the AuthenticatorFactory.
     */
    static {
        AuthenticatorFactory.getInstance().registerAuthenticator(
                GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE, new GoogleAuthenticator());
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public Authenticator newInstance() {
        return new GoogleAuthenticator();
    }

    /**
     * TODO: Add specific doc.
     *
     * @param caller Activity requesting the token (used in case user interaction is needed).
     * @param account Account to get the token for.
     * @return
     */
    @Override
    public String getToken(Activity caller, Account account) {
        try {
            return GoogleAuthUtil.getToken(caller, account.name, "oauth2:https://mail.google.com/");
        } catch (final GooglePlayServicesAvailabilityException e) {
            // TODO: Find a way to make calling activity show the dialog.
            //showGooglePlayServicesDialog(e.getConnectionStatusCode());
        } catch (UserRecoverableAuthException e) {
            // Start the user recoverable action using the intent returned by getIntent()
            caller.startActivityForResult(e.getIntent(), Authenticator.GET_AUTH_TOKEN_REQUEST);
        } catch (IOException e) {
            // Network or server error, should retry but not immediately.
            LOGW(TAG, e.toString(), e);
        } catch (GoogleAuthException e) {
            // Failure, call is not expected to ever succeed. It should not be retried.
            LOGE(TAG, e.toString(), e);
        }

        return null;
    }


    @Override
    public void invalidateToken(Context context, String token) {
        GoogleAuthUtil.invalidateToken(context, token);
    }
}
