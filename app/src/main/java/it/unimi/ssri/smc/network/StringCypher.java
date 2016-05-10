package it.unimi.ssri.smc.network;

import android.util.Base64;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

public class StringCypher {

    private static final String ASYM_CYPHER_NAME = "RSA";

    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ASYM_CYPHER_NAME);
        kpg.initialize(1024);
        KeyPair kp = kpg.genKeyPair();
        return kp;
    }

    public static String encrypt(String plain, PublicKey publicKey) throws GeneralSecurityException {
            Cipher cipher = Cipher.getInstance(ASYM_CYPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plain.getBytes());
            String encrypted = bytesToString(encryptedBytes);
            return encrypted;
    }

    public static String encrypt(String plain, String publicKeyStr) throws GeneralSecurityException {
            PublicKey externalPublicKey = loadPublicKey(publicKeyStr);
            return encrypt(plain, externalPublicKey);
    }

    public static String decrypt(String result, PrivateKey privateKey) throws GeneralSecurityException{
            Cipher cipher1 = Cipher.getInstance(ASYM_CYPHER_NAME);
            cipher1.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher1.doFinal(stringToBytes(result));
            String decrypted = new String(decryptedBytes);
            return decrypted;
    }

    public static String decrypt(String result, String privateKeyStr) throws GeneralSecurityException{
        PrivateKey privateKey = loadPrivateKey(privateKeyStr);
        return decrypt(result, privateKey);
    }

    private static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    private static byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }

    public static PrivateKey loadPrivateKey(String key64)  throws GeneralSecurityException{
        byte[] clear = Base64.decode(key64, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance(ASYM_CYPHER_NAME);
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    public static PublicKey loadPublicKey(String pubString) throws GeneralSecurityException{
        byte[] data = Base64.decode(pubString, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance(ASYM_CYPHER_NAME);
        return fact.generatePublic(spec);
    }

    public static String getPrivateKeyString(PrivateKey priv) throws GeneralSecurityException{
        KeyFactory fact = KeyFactory.getInstance(ASYM_CYPHER_NAME);
        PKCS8EncodedKeySpec spec = fact.getKeySpec(priv, PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = Base64.encodeToString(packed, Base64.DEFAULT);

        Arrays.fill(packed, (byte) 0);
        return key64;
    }

    public static String getPublicKeyString(PublicKey publ) throws GeneralSecurityException{
        KeyFactory fact = KeyFactory.getInstance(ASYM_CYPHER_NAME);
        X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
        return Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
    }
}
