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
     * Multipart marker.
     */
    private static final String MARKER = "1234THISISAUNIQUEMARKER4321";

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

    /**
     * Returns a string representation of this email.
     *
     * @return String representation of this email.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        // Assemble recipients field.
        final StringBuilder recipients = new StringBuilder();
        String glue = "";

        for (String recipient : mRecipients) {
            recipients.append(glue).append(recipient);

            if (glue.length() == 0) {
                glue = ", ";
            }
        }

        // Build SMTP header.
        final Multipart headerPart = new Multipart();
        headerPart.addHeaderField("From", mSender);
        headerPart.addHeaderField("To", recipients.toString());
        headerPart.addHeaderField("Subject", mSubject);
        headerPart.addHeaderField("MIME-Version", "1.0");
        headerPart.addHeaderField("Content-Type", "multipart/mixed; boundary=" + MARKER);

        sb.append(headerPart.toString());

        // Build the message part.
        if (mBody.length() > 0) {
            sb.append("--").append(MARKER).append("\n");

            final Multipart messagePart = new Multipart();
            final DataSource dataSource = new TextDataSource(mBody);
            messagePart.addHeaderField("Content-Type", dataSource.getContentType());
            messagePart.addHeaderField("Content-Transfer-Encoding", "8bit");
            messagePart.setDataSource(dataSource);

            sb.append(messagePart.toString());
        }

        for (Attachment attachment : mAttachements) {
            sb.append("--").append(MARKER).append("\n");

            final Multipart messagePart = new Multipart();
            final DataSource dataSource = attachment.getDataSource();
            messagePart.addHeaderField("Content-Type", dataSource.getContentType());
            messagePart.addHeaderField("Content-Transfer-Encoding", "base64");
            messagePart.addHeaderField("Content-Disposition", "attachement; filename=\"" + dataSource.getName() + "\"");
            messagePart.setDataSource(dataSource);

            sb.append(messagePart.toString());
        }

        sb.append("--").append(MARKER).append("--");

        return sb.toString();
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
