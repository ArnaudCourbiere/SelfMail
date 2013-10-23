package com.javatomic.drupal.mail;

import android.net.Uri;

/**
 * Email attachment.
 */
public class Attachment {
    private static final String TAG = "Attachment";

    private String mName;
    private Uri mUri;
    private String mPath;

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }
}
