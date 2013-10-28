package com.javatomic.drupal.mail;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an email.
 * This class is parcelable in order to be add to a SendEmailService intent.
 */
public class Email implements Parcelable {
    private static final String TAG = "Email";

    /**
     * Email sender.
     */
    private String mSender;

    /**
     * Email recipients.
     */
    private List<String> mRecipients;

    /**
     * Email subject.
     */
    private String mSubject;

    /**
     * Email body.
     */
    private String mBody;

    /**
     * Email attachments.
     */
    private List<Attachment> mAttachements;

    /**
     * Initializes a new Email.
     */
    public Email() {
        mSubject = "";
        mBody = "";
        mRecipients = new ArrayList<String>();
        mAttachements = new ArrayList<Attachment>();
    }

    /**
     * Sets the email sender.
     *
     * @param sender The email sender.
     */
    public void setSender(String sender) {
        mSender = sender;
    }

    /**
     * Retrieves the sender of the email.
     *
     * @return The sender of the email.
     */
    public String getSender() {
        return mSender;
    }

    /**
     * Adds a recipient to the email.
     *
     * @param recipient A recipient for the email.
     */
    public void addRecipient(String recipient) {
        mRecipients.add(recipient);
    }

    /**
     * Retrieves the list of recipients of the email.
     *
     * @return The list of recipients.
     */
    public List<String> getRecipients() {
        return mRecipients;
    }

    /**
     * Sets the email subject.
     *
     * @param subject Email subject.
     */
    public void setSubject(String subject) {
        mSubject = subject;
    }

    /**
     * Retrieves the subject of the email.
     *
     * @return The email subject.
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Sets the email body.
     *
     * @param body Email body.
     */
    public void setBody(String body) {
        mBody = body;
    }

    /**
     * Retrieves the body of the email.
     *
     * @return The email body.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Add a new attachment to the email.
     *
     * @param attachment The attachment to add to the email.
     */
    public void addAttachment(Attachment attachment) {
        mAttachements.add(attachment);
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
        out.writeString(mSender);
        out.writeValue(mRecipients);
        out.writeString(mSubject);
        out.writeString(mBody);
        out.writeValue(mAttachements);
    }

    /**
     * Email static creator.
     */
    public static final Creator<Email> CREATOR = new Creator<Email>() {

        /**
         * Create a new Email instance, instantiating it from the given Parcel
         * whose data had previously been written by Email.writeToParcel().
         *
         * @param source The Parcel to read the Email's data from.
         * @return Returns a new Email instance.
         */
        @Override
        public Email createFromParcel(Parcel source) {
            return new Email(source);
        }

        /**
         * Creates a new array of Email class.
         *
         * @param size Size of the array.
         * @return An array of Email class with every entry initialized to null.
         */
        @Override
        public Email[] newArray(int size) {
            return new Email[size];
        }
    };

    /**
     * Constructor needed to build object from parcel.
     *
     * @param in Parcel to read data from.
     */
    private Email(Parcel in) {
        mSender = in.readString();
        mRecipients = (List<String>) in.readValue(Email.class.getClassLoader());
        mSubject = in.readString();
        mBody = in.readString();
        mAttachements = (List<Attachment>) in.readValue(Email.class.getClassLoader());
    }
}
