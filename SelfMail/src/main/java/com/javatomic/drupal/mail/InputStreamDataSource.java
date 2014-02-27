package com.javatomic.drupal.mail;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.javatomic.drupal.util.ParcelFileDescriptorUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Data source reading from an {@link InputStream}.
 */
public class InputStreamDataSource extends DataSource {
    private static final String TAG = "InputStreamDataSource";

    private String mName;
    private InputStream mInputStream;

    public InputStreamDataSource(String name, InputStream inputStream) {
        mName = name;
        mInputStream = inputStream;
    }

    @Override
    public String getContentType() {
        String mimeType = "multipart/mixed";

        try {
            mimeType = URLConnection.guessContentTypeFromStream(mInputStream);
        } catch (IOException e) {
            LOGW(TAG, e.toString());
        }
        return String.format("%s; name=\"%s\"", mimeType, getName());
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getData() {
        ByteArrayOutputStream baos = null;
        Base64OutputStream base64out = null;
        String content = null;

        try {
            baos = new ByteArrayOutputStream();
            base64out = new Base64OutputStream(baos, Base64.DEFAULT);
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = mInputStream.read(data, 0, data.length)) != -1) {
                base64out.write(data, 0, nRead);
            }

            content = new String(baos.toByteArray());
        } catch (IOException e) {
            LOGE(TAG, e.toString(), e);
        } finally {
            try {
                if (mInputStream != null) {
                    mInputStream.close();
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
        return CONTENTS_FILE_DESCRIPTOR;
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
        out.writeString(mName);
        try {
            // TODO: Handle error saying cannot write file descriptor here.
            out.writeValue(ParcelFileDescriptorUtil.pipeFrom(mInputStream));
        } catch (IOException e) {
            LOGE(TAG, e.toString(), e);
        }
    }

    /**
     * Static creator, required for parcelables.
     */
    public static final Creator<InputStreamDataSource> CREATOR = new Creator<InputStreamDataSource>() {

        /**
         * Create a new instance of the Parcelable class, instantiating it from the given Parcel
         * whose data had previously been written by writeToParcel().
         *
         * @param source The Parcel to read the data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public InputStreamDataSource createFromParcel(Parcel source) {
            return new InputStreamDataSource(source);
        }

        /**
         * Creates a new array of TextDataSource class.
         *
         * @param size Size of the array.
         * @return An array of TextDataSource class with every entry initialized to null.
         */
        @Override
        public InputStreamDataSource[] newArray(int size) {
            return new InputStreamDataSource[size];
        }
    };

    /**
     * Constructor needed to build object from parcel.
     *
     * @param in Parcel to read data from.
     */
    private InputStreamDataSource(Parcel in) {
        mName = in.readString();
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) in.readValue(InputStreamDataSource.class.getClassLoader());
        mInputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
    }
}
