package com.javatomic.drupal.mail;

import android.net.Uri;

public class UriDataSource implements DataSource {
    private static final String TAG = "UriDataSource";
    private Uri mUri;

    public UriDataSource(Uri uri) {
        mUri = uri;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getData() {
        return null;
    }
}
