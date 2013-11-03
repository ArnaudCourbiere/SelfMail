package com.javatomic.drupal.mail;

import android.os.Parcel;

public class TextDataSource extends DataSource {
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

    /* Parcelable related functionalities */

    /**
     * Describe the kinds of special objects contained in this Parcelable's marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param out The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mText);
    }

    /**
     * Static creator, required for parcelables.
     */
    public static final Creator<TextDataSource> CREATOR = new Creator<TextDataSource>() {

        /**
         * Create a new instance of the Parcelable class, instantiating it from the given Parcel
         * whose data had previously been written by writeToParcel().
         *
         * @param source The Parcel to read the data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public TextDataSource createFromParcel(Parcel source) {
            return new TextDataSource(source);
        }

        /**
         * Creates a new array of TextDataSource class.
         *
         * @param size Size of the array.
         * @return An array of TextDataSource class with every entry initialized to null.
         */
        @Override
        public TextDataSource[] newArray(int size) {
            return new TextDataSource[size];
        }
    };

    /**
     * Constructor needed to build object from parcel.
     *
     * @param in Parcel to read data from.
     */
    private TextDataSource(Parcel in) {
        mText = in.readString();
    }
}
