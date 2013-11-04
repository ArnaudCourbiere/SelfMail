package com.javatomic.drupal.ui.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.javatomic.drupal.auth.Authenticator;
import com.javatomic.drupal.auth.AuthenticatorFactory;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.service.SendEmailService;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * TODO
 */
public class SendEmailAsyncTask extends AsyncTask<Email, Void, Boolean> {
    private static final String TAG = "SendEmailAsyncTask";

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
    }

    @Override
    protected Boolean doInBackground(Email... params) {
        if (params.length == 1) {
            Email email = params[0];

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
    } else {
            LOGW(TAG, "Background task can only execute one email, " +
                    "multiple emails passed to the call to execute()");
        }

        return false;
    }
}
