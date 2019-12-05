package com.alexsci.android.lambdarunner.util.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Secure storage of credentials
 *
 * Allows storage of credentials by the app.
 *
 * Steps:
 *  - Credentials are encrypted (AES256/GCM) then stored in SharedPreferences
 *  - The AndroidKeyStore manages the root encryption key
 *  - The AndroidKeyStore asks for the user to authenticate
 *  - The AndroidKeyStore ensures that the user is present
 *
 */
public class KeyManagement {
    /*
     * AES256-GCM
     *
     * Why:
     *  - We don't need separate public/private keys
     *  - AES is a strong symmetric encryption algorithm.
     *  - 256 is the largest key size supported by the AES standard.
     *  - GCM is a strong block mode
     *  - GCM provides authenticity
     *  - We don't need separate padding for GCM
     *
     * Key is only needed for encrypt/decrypt
     */
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    private static final String KEY_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    private static final String KEY_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    private static final int KEY_SIZE = 256;
    private static final int KEY_PROPERTIES = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;

    private static final String KEY_STORE_PROVIDER = "AndroidKeyStore";
    private static final String SHARED_PREFERENCES_FILE = "KeyManagement";
    private static final String ROOT_KEY = "APP_ROOT_KEY";

    private final SharedPreferences sharedPreferences;
    private final KeyStore keyStore;

    private KeyManagement(Context context) throws GeneralSecurityException, IOException {
        this.sharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

        this.keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER);
        this.keyStore.load(null);

        if (!hasRootKey()) {
            createRootKey();
        }
    }

    private static KeyManagement keyManagementInstance = null;

    synchronized
    public static KeyManagement getInstance(Context context) throws GeneralSecurityException, IOException {
        if (keyManagementInstance == null) {
            context = context.getApplicationContext();
            keyManagementInstance = new KeyManagement(context);
        }
        return keyManagementInstance;
    }

    private void createRootKey() {
        Log.i("RAA", "Creating a root key");

        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KEY_ALGORITHM, KEY_STORE_PROVIDER);

            final KeyGenParameterSpec.Builder keyGenParameterSpecBuilder =
                    new KeyGenParameterSpec.Builder(ROOT_KEY, KEY_PROPERTIES)
                            .setKeySize(KEY_SIZE)
                            .setBlockModes(KEY_BLOCK_MODE)
                            .setEncryptionPaddings(KEY_PADDING);

            // Need to handle fingerprint, iris, intelligentScan not enrolled to enable this
            keyGenParameterSpecBuilder.setUserAuthenticationRequired(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                keyGenParameterSpecBuilder.setUserPresenceRequired(true);
            }

            final KeyGenParameterSpec keyGenParameterSpec = keyGenParameterSpecBuilder.build();

            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        Log.i("RAA", "Root key created successfully");
    }

    private boolean hasRootKey() throws GeneralSecurityException {
        boolean result = keyStore.isKeyEntry(ROOT_KEY);
        Log.i("RAA", "Has Root Key: " + result);
        return result;
    }

    private Key getRootKey() throws GeneralSecurityException {
        return keyStore.getKey(ROOT_KEY, null);
    }

    private void deleteRootKey() throws KeyStoreException {
        Log.i("RAA", "Deleting root key...");
        keyStore.deleteEntry(ROOT_KEY);
        Log.i("RAA", "Deleted");
    }

    public Collection<String> listKeys() {
        return sharedPreferences.getAll().keySet();
    }

    public boolean addKey(String name, String description, String value) throws GeneralSecurityException {
        EncryptedResult result = encrypt(value);
        BasicCredentialInformation info = new BasicCredentialInformation(name, description, result.getIv(), result.getEncrypted());
        return sharedPreferences.edit().putString(name, info.toJson()).commit();
    }

    public boolean hasKey(String name) {
        return sharedPreferences.contains(name);
    }

    public BasicCredentialInformation describeKey(String name) throws GeneralSecurityException {
        String storedValue = sharedPreferences.getString(name, null);
        if (storedValue == null) {
            throw new InvalidKeyException("Can't find " + name);
        }
        return new BasicCredentialInformation(storedValue);
    }

    public String getKey(String name) throws GeneralSecurityException {
        BasicCredentialInformation info = describeKey(name);
        return decrypt(info.getIv(), info.getEncrypted());
    }

    public boolean deleteKey(String name) {
        return sharedPreferences.edit().remove(name).commit();
    }

    private Cipher getCipherInstance() throws GeneralSecurityException {
        return Cipher.getInstance(CIPHER_TRANSFORMATION);
    }

    private EncryptedResult encrypt(String text) throws GeneralSecurityException {
        final Cipher cipher = getCipherInstance();
        cipher.init(Cipher.ENCRYPT_MODE, getRootKey());
        final byte[] iv = cipher.getIV();
        final byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        return new EncryptedResult(iv, encrypted);
    }

    private String decrypt(byte[] iv, byte[] encrypted) throws GeneralSecurityException {
        final Cipher cipher = getCipherInstance();
        final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, getRootKey(), spec);

        final byte[] plaintext = cipher.doFinal(encrypted);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
