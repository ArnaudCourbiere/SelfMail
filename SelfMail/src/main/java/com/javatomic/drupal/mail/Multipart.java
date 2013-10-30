package com.javatomic.drupal.mail;

import android.net.Uri;

import com.google.android.gms.internal.u;

/**
 * SMTP Email Multipart.
 */
public class Multipart {
    private static final String TAG = "Multipart";

    /**
     * StringBuilder used to assemble Multipart headers.
     */
    private StringBuilder mHeaders;

    /**
     * Part data source.
     */
    private DataSource mDataSource;

    /**
     * Creates a new {@link Multipart}.
     */
    public Multipart() {
        mHeaders = new StringBuilder();
    }

    /**
     * Adds a new header field with the given value to the Multipart.
     * @param name
     * @param value
     */
    public void addHeaderField(String name, String value) {
        mHeaders.append(name).append(": ").append(value).append("\n");
    }

    /**
     * Sets the {@link DataSource} of this multipart.
     */
    public void setDataSource(DataSource dataSource) {
        mDataSource = dataSource;
    }

    /**
     * Converts the Multipart to a properly formatted SMTP Multipart.
     *
     * @return The Part String representation.
     */
    @Override
    public String toString() {
        if (mDataSource != null) {
            mHeaders.append("\n").append(mDataSource.getData()).append("\n");
        }

        return mHeaders.toString();
    }
}
