package com.javatomic.drupal.auth;

import java.util.HashMap;

import static com.javatomic.drupal.util.LogUtils.*;

/**
 * Factory class used to register and get Authenticators for email accounts.
 */
public class AuthenticatorFactory {
    private static final String TAG = "AuthenticatorFactory";

    /**
     * Singleton instance of the {@link AuthenticatorFactory}.
     */
    private static AuthenticatorFactory sSingleton = new AuthenticatorFactory();

    /**
     * Map of registered Authenticators keyed by their type.
     */
    private HashMap<String, Authenticator> mRegisteredAuthenticators;

    // Load Authenticators.
    // TODO: Read from resource file.
    static {
        try {
            Class.forName("com.javatomic.drupal.auth.concrete.GoogleAuthenticator");
        } catch (ClassNotFoundException e) {
            LOGE(TAG, e.toString(), e);
        }
    }

    /**
     * Private constructor, initializes the HashMap of registered {@link Authenticator}.
     */
    private AuthenticatorFactory() {
        mRegisteredAuthenticators = new HashMap<String, Authenticator>();
    }

    /**
     * Retrieves the singleton instance of the {@link AuthenticatorFactory}.
     *
     * @return Singleton instance.
     */
    public static AuthenticatorFactory getInstance() {
        return sSingleton;
    }

    /**
     * Registers a type of {@link Authenticator}.
     *
     * @param type Authenticator type (usually the account type).
     * @param authenticator {@link Authenticator} instance.
     * @throws IllegalArgumentException If the account type specified is already registered.
     */
    public void registerAuthenticator(String type, Authenticator authenticator) {
        if (mRegisteredAuthenticators.containsKey(type)) {
            throw new IllegalArgumentException(
                    String.format("This type of account, %s, is already registered.", type));
        }

        mRegisteredAuthenticators.put(type, authenticator);
    }

    /**
     * Creates and returns a new instance of the specified {@link Authenticator}.
     *
     * @param type The type of {@link Authenticator} requested.
     * @return A new instance of the {@link Authenticator} or null if none of that type is registered.
     */
    public Authenticator createAuthenticator(String type) {
        final Authenticator authenticator =  mRegisteredAuthenticators.get(type);

        if (authenticator != null) {
            return authenticator.newInstance();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the specified account type is supported, false otherwise.
     *
     * @param type Account type.
     * @return Returns true if the specified account type is supported, false otherwise.
     */
    public boolean isSupportedAccountType(String type) {
        return mRegisteredAuthenticators.containsKey(type);
    }
}
