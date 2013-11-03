package com.javatomic.drupal.mail;

import android.os.Parcelable;

public abstract class DataSource implements Parcelable {
    public abstract String getContentType();
    public abstract String getName();
    public abstract String getData();
}
