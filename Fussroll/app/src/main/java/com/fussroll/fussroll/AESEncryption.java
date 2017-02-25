package com.fussroll.fussroll;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static android.util.Base64.decode;
import static android.util.Base64.encodeToString;

/**
 * Created by kunal on 29/12/16.
 */

class AESEncryption {

    private static final String algorithm = "AES";
    private static final byte[] ring = "llorssuf#1#2#3@a".getBytes();

    static String encrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return (encodeToString(encryptedData, 16));
    }

    static String decrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = decode(data, 16);
        return new String(cipher.doFinal(decodedData));
    }

    private static Key generateKey() {
        return new SecretKeySpec(ring, algorithm);

    }
}

