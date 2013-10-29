package com.javatomic.drupal.mail;

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
     * Part content.
     */
    private String mContent;

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
     * Sets the content of this multipart.
     */
    public void setContent(String content) {
        mContent = content;
    }

    /**
     * Converts the Multipart to a properly formatted SMTP Multipart.
     *
     * @return The Part String representation.
     */
    @Override
    public String toString() {
        if (mContent != null) {
            mHeaders.append("\n").append(mContent).append("\n");
        }

        return mHeaders.toString();
    }
}
