package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.auth.Authenticator;
import com.javatomic.drupal.auth.AuthenticatorFactory;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.net.NetworkReceiver;
import com.javatomic.drupal.service.SendEmailService;

import java.util.ArrayList;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Activity responsible for receiving the intent from the share action, retrieve the associated
 * data and start the SendEmail service.
 */
public class ShareDataActivity extends Activity {
    private static final String TAG = "ShareDataActivity";

    /**
     * Email being sent.
     */
    private Email mEmail;

    /**
     * Users currently selected account.
     */
    private Account mChosenAccount;

    /**
     * Retrieve data from the intent that started the activity and starts the SendEmail service.
     *
     * @param savedInstanceState If the {@link Activity} is being re-initialized after being
     *     shut down, this {@link Bundle} contains the data most recently supplied in
     *     {@link #onSaveInstanceState(android.os.Bundle)}, it is null otherwise.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        mChosenAccount = AccountUtils.getChosenAccount(this);
        mEmail = new Email();
        mEmail.setSender(mChosenAccount.name);
        mEmail.addRecipient(mChosenAccount.name);
        mNetworkReceiver = new NetworkReceiver(this);

        if (action.equals(Intent.ACTION_SEND) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText(intent);
            } else if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        } else if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent);
            }
        } else {
            // TODO Show error message.
            finish();
        }

        sendEmail();
    }

    /**
     * Called when the user either has installed Google Play Services or allowed SelfMail authorization
     * to access its email account.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data Data returned to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            LOGD(TAG, "User canceled operation, request code: " + requestCode);
        } else {
            switch (requestCode) {
                //case ComposeActivity.INSTALL_PLAY_SERVICES_REQUEST:
                case Authenticator.GET_AUTH_TOKEN_REQUEST:
                    this.sendEmail();
                    break;
            }
        }
    }

    /**
     * Parses {@link Intent#EXTRA_TEXT} and {@link Intent#EXTRA_SUBJECT} from the specified intent
     * and sets them as body and subject of the Activity email.
     *
     * @param intent Intent that started this activity.
     */
    private void handleSendText(Intent intent) {
        final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        if (sharedText != null) {
            if (sharedSubject == null) {
                sharedSubject = "SelfMail";
            }

            mEmail.setSubject(sharedSubject);
            mEmail.setBody(sharedText);
        }
    }

    /**
     * Parses {@link Intent#EXTRA_STREAM} from the specified intent and add the decoded image as an
     * attachment to the activity email.
     *
     * @param intent Intent that started this activity.
     */
    private void handleSendImage(Intent intent) {
        final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    /**
     * Parses {@link Intent#EXTRA_STREAM} from the specified intent and add the decoded images as
     * attachments to the activity email.
     *
     * @param intent Intent that started this activity.
     */
    private void handleSendMultipleImages(Intent intent) {
        final ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }

    /**
     * TODO
     */
    public void sendEmail() {
        // Get auth token in worker thread.
        AsyncTask<Email, Void, Boolean> task = new AsyncTask<Email, Void, Boolean>() {

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
        };

        task.execute(mEmail);
    }
}
