package com.javatomic.drupal.mail;

public interface DataSource {
    public String getContentType();
    public String getName();
    public String getData();
}
