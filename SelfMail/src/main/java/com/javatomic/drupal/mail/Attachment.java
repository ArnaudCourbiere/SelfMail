package com.javatomic.drupal.mail;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Email attachment.
 */
public class Attachment implements Parcelable {
    private static final String TAG = "Attachment";

    private DataSource mDataSource;

    /**
     * Creates a new attachment with the specified name and the specified {@link DataSource}.
     *
     * @param dataSource Attachment {@link DataSource}.
     */
    public Attachment(DataSource dataSource) {
        mDataSource = dataSource;
    }

    public DataSource getDataSource() {
        return mDataSource;
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
        out.writeValue(mDataSource);
    }

    /**
     * Attachment static creator.
     */
    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {

        /**
         * Create a new Attachment instance, instantiating it from the given Parcel
         * whose data had previously been written by Attachment.writeToParcel().
         *
         * @param source The Parcel to read the Attachment's data from.
         * @return Returns a new Attachment instance.
         */
        @Override
        public Attachment createFromParcel(Parcel source) {
            return new Attachment(source);
        }

        /**
         * Creates a new array of Attachment class.
         *
         * @param size Size of the array.
         * @return An array of Attachment class with every entry initialized to null.
         */
        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    /**
     * Constructor needed to build object from parcel.
     *
     * @param in Parcel to read data from.
     */
    private Attachment(Parcel in) {
        mDataSource = (DataSource) in.readValue(Attachment.class.getClassLoader());
    }
}
