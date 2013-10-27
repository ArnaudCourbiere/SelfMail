package com.javatomic.drupal.ui.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.javatomic.drupal.auth.Authenticator;
import com.javatomic.drupal.auth.AuthenticatorFactory;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.net.NetworkReceiver;
import com.javatomic.drupal.service.SendEmailService;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * TODO
 */
public class SendEmailAsyncTask extends AsyncTask<Email, Void, Boolean> {
    private static final String TAG = "SendEmailAsyncTask";

    /**
     * Listener for network connectivity changes.
     */
    private NetworkReceiver mNetworkReceiver;

    /**
     * Calling {@link Activity}.
     */
    private Activity mCaller;

    /**
     * User account
     */
    Account mAccount;

    /**
     * TODO
     *
     * @param caller
     */
    public SendEmailAsyncTask(Activity caller, Account account) {
        mCaller = caller;
        mAccount = account;
        mNetworkReceiver = new NetworkReceiver(caller);

        // Register listener for network state changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver(caller);
        caller.registerReceiver(mNetworkReceiver, filter);
    }

    @Override
    protected Boolean doInBackground(Email... params) {
        if (params.length == 1) {
            Email email = params[0];

            try {
                mNetworkReceiver.waitForNetwork();
                final Authenticator authenticator = AuthenticatorFactory
                        .getInstance().createAuthenticator(mAccount.type);
                final String token = authenticator.getToken(mCaller, mAccount);

                if (token != null) {
                    final Intent intent = new Intent(mCaller, SendEmailService.class);
                    intent.putExtra(SendEmailService.EMAIL, email);
                    intent.putExtra(SendEmailService.AUTH_TOKEN, token);
                    mCaller.startService(intent);

                    return true;
                }
            } catch (InterruptedException e) {
                LOGE(TAG, e.toString(), e);
            }
        } else {
            LOGW(TAG, "Background task can only execute one email, " +
                    "multiple emails passed to the call to execute()");
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (mNetworkReceiver != null) {
            mCaller.unregisterReceiver(mNetworkReceiver);
        }
    }
}
