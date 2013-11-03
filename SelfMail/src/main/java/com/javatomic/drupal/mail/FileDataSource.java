package com.javatomic.drupal.mail;

import android.os.Parcel;
import android.util.Base64;
import android.util.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

import static com.javatomic.drupal.util.LogUtils.*;

public class FileDataSource extends DataSource {
    private static final String TAG = "FileDataSource";
    private File mFile;

    public FileDataSource(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist.");
        }

        mFile = file;
    }

    @Override
    public String getContentType() {
        final String mimeType = URLConnection.guessContentTypeFromName(getName());
        return String.format("%s; name=\"%s\"", mimeType, getName());
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getData() {
        ByteArrayOutputStream baos = null;
        Base64OutputStream base64out = null;
        FileInputStream fis = null;
        String content = null;

        try {
            baos = new ByteArrayOutputStream();
            base64out = new Base64OutputStream(baos, Base64.DEFAULT);
            fis = new FileInputStream(mFile);
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = fis.read(data, 0, data.length)) != -1) {
                base64out.write(data, 0, nRead);
            }

            content = new String(baos.toByteArray());
        } catch (IOException e) {
            LOGE(TAG, e.toString(), e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (base64out != null) {
                    base64out.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                LOGW(TAG, e.toString(), e);
            }
        }

        return content;
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
        out.writeValue(mFile);
    }

    /**
     * Static creator, required for parcelables.
     */
    public static final Creator<FileDataSource> CREATOR = new Creator<FileDataSource>() {

        /**
         * Create a new instance of the Parcelable class, instantiating it from the given Parcel
         * whose data had previously been written by writeToParcel().
         *
         * @param source The Parcel to read the data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public FileDataSource createFromParcel(Parcel source) {
            return new FileDataSource(source);
        }

        /**
         * Creates a new array of TextDataSource class.
         *
         * @param size Size of the array.
         * @return An array of TextDataSource class with every entry initialized to null.
         */
        @Override
        public FileDataSource[] newArray(int size) {
            return new FileDataSource[size];
        }
    };

    /**
     * Constructor needed to build object from parcel.
     *
     * @param in Parcel to read data from.
     */
    private FileDataSource(Parcel in) {
        mFile = (File) in.readValue(FileDataSource.class.getClassLoader());
    }
}
