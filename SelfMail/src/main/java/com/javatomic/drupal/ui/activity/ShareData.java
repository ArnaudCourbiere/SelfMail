package com.javatomic.drupal.ui.activity;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.javatomic.drupal.account.AccountUtils;
import com.javatomic.drupal.mail.Email;

import java.util.ArrayList;

/**
 * Activity responsible for receiving the intent from the share action, retrieve the associated
 * data and start the SendEmail service.
 */
public class ShareData extends Activity {
    private static final String TAG = "ShareData";

    /**
     * Email being sent.
     */
    private Email mEmail;

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

        final Account account = AccountUtils.getChosenAccount(this);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        mEmail = new Email();
        mEmail.setSender(account.name);
        mEmail.addRecipient(account.name);

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
        }

        // Get auth token.

        // Start SendEmail service.
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
}
