package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.javatomic.drupal.R;
import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.auth.Authenticator;
import com.javatomic.drupal.auth.AuthenticatorFactory;
import com.javatomic.drupal.mail.Attachment;
import com.javatomic.drupal.mail.DataSource;
import com.javatomic.drupal.mail.Email;
import com.javatomic.drupal.mail.FileDataSource;
import com.javatomic.drupal.mail.InputStreamDataSource;
import com.javatomic.drupal.net.NetworkReceiver;
import com.javatomic.drupal.service.SendEmailService;
import com.javatomic.drupal.ui.util.SendEmailAsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Activity responsible for receiving the intent from the share action, retrieve the associated
 * data and start the SendEmail service.
 */
public class ShareDataActivity extends Activity {
    private static final String TAG = "ShareDataActivity";

    private static final int INSTALL_PLAY_SERVICES_REQUEST = 1000;

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

        // printExtras(intent);

        mChosenAccount = AccountUtils.getChosenAccount(this);
        mEmail = new Email();
        mEmail.setSender(mChosenAccount.name);
        mEmail.addRecipient(mChosenAccount.name);

        if (action.equals(Intent.ACTION_SEND) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText(intent);
            } else if (type.startsWith("image/")) {
                mEmail.setSubject(getString(R.string.app_name));
                handleSendImage(intent);
            }
        } else if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
            if (type.startsWith("image/")) {
                mEmail.setSubject(getString(R.string.app_name));
                handleSendMultipleImages(intent);
            }
        } else {
            // TODO Show error message?.
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
            finish();
        } else {
            switch (requestCode) {
                case INSTALL_PLAY_SERVICES_REQUEST:
                case Authenticator.GET_AUTH_TOKEN_REQUEST:
                    this.sendEmail();
                    break;
                default:
                    finish();
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

        if (intent.hasExtra("share_screenshot")) {
            Object screenshotExtra = intent.getParcelableExtra("share_screenshot");

            if (screenshotExtra instanceof Bitmap) {
                // TODO: Add bitmap as attachment.
            }
        }

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
        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            final File file = getFile(imageUri);

            if (file != null) {
                mEmail.addAttachment(new Attachment(new FileDataSource(file)));
            } else {
                Toast.makeText(this, R.string.error_adding_attachment, Toast.LENGTH_SHORT).show();
            }
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
            for (Uri imageUri : imageUris) {
                final File file = getFile(imageUri);

                if (file != null) {
                    mEmail.addAttachment(new Attachment(new FileDataSource(file)));
                }
            }
        }
    }

    /**
     * Retrieves the {@link File} associated with a {@link Uri}. Returns null if not image is found.
     *
     * @param uri The {@link Uri} to retrieve the {@link File} from.
     * @return The {@link File} or null if no File is found.
     */
    private File getFile(Uri uri) {
        File file = null;
        String[] projection = { MediaStore.Images.ImageColumns.DATA };
        Cursor c = getContentResolver().query(uri, projection, null, null, null);

        if (c != null && c.moveToFirst()) {
            String filePath = c.getString(0);
            file = new File(filePath);

            if (!file.exists()) {
                file = null;
            }
        }

        return file;
    }

    /**
     * Checks that Google Play Services are available on the device. Shows the dialog to install
     * Google Play Services if they are not available.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionsStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (GooglePlayServicesUtil.isUserRecoverableError(connectionsStatusCode)) {
            showGooglePlayServicesDialog(connectionsStatusCode);

            return false;
        }

        return true;
    }

    /**
     * Show the dialog that prompt the user to install Google Play Services.
     *
     * @param statusCode Status from the UserRecoverableError.
     */
    private void showGooglePlayServicesDialog(final int statusCode) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                        statusCode, ShareDataActivity.this, INSTALL_PLAY_SERVICES_REQUEST);

                if (alert == null) {
                    final String errorMessage = getResources().getString(R.string.incompatible_google_play);
                    Toast.makeText(ShareDataActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                alert.show();
            }
        });
    }

    /**
     * TODO
     */
    public void sendEmail() {
        final boolean googlePlayServicesAvailable = checkGooglePlayServicesAvailable();

        if (googlePlayServicesAvailable) {
            // Get auth token in worker thread.
            SendEmailAsyncTask task = new SendEmailAsyncTask(this, mChosenAccount) {

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);

                    if (success) {
                        Toast.makeText(ShareDataActivity.this, getString(R.string.sending_selfmail), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            };

            task.execute(mEmail);
        }
    }

    private void printExtras(Intent intent) {
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            LOGD(TAG, String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        }
    }
}
