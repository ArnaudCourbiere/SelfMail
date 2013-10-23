package com.javatomic.drupal.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;

public class ShareData extends Activity {
    private static final String TAG = "ShareData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

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
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }
}
