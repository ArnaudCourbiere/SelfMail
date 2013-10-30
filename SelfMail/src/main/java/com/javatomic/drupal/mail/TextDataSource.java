package com.javatomic.drupal.mail;

public class TextDataSource implements DataSource {
    private static final String TAG = "TextDataSource";
    private String mText;

    public TextDataSource(String text) {
        mText = text;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public String getName() {
        return "TextDataSource";
    }

    @Override
    public String getData() {
        return mText;
    }
}
