package com.javatomic.drupal.ui.util;

import android.os.AsyncTask;

import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.net.NetworkReceiver;

/**
 * TODO
 */
public class SendEmailAsyncTask extends AsyncTask<Email, Void, Boolean> {
    private static final String TAG = "SendEmailAsyncTask";

    /**
     * Listener for network connectivity changes.
     */
    private NetworkReceiver mNetworkReceiver;

    public SendEmailAsyncTask(Actiity) {
        mNetworkReceiver = new NetworkReceiver();
    }

    @Override
    protected Boolean doInBackground(Email... params) {
        if (params.length == 1) {
            Email email = params[0];

            try {
                mNetworkReceiver.waitForNetwork();
                final Authenticator authenticator = AuthenticatorFactory
                        .getInstance().createAuthenticator(mChosenAccount.type);
                final String token = authenticator.getToken(ShareDataActivity.this, mChosenAccount);

                if (token != null) {
                    final Intent intent = new Intent(ShareDataActivity.this, SendEmailService.class);
                    intent.putExtra(SendEmailService.EMAIL, email);
                    intent.putExtra(SendEmailService.AUTH_TOKEN, token);
                    startService(intent);

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
        if (success) {
            Toast.makeText(ShareDataActivity.this, getString(R.string.sending_selfmail), Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
