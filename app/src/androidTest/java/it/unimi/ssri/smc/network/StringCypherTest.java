package it.unimi.ssri.smc.network;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

@RunWith(AndroidJUnit4.class)
public class StringCypherTest {

    @Test
    public void simplyCryptDecrypt() throws GeneralSecurityException{
        KeyPair kp = StringCypher.generateKeyPair();
        String plain = "this is a test";
        String cypherText = StringCypher.encrypt(plain,kp.getPublic());
        String deCypherText = StringCypher.decrypt(cypherText, kp.getPrivate());
        Assert.assertEquals(plain,deCypherText);
    }

    @Test
    public void simplyCryptDecryptFalse() throws GeneralSecurityException{
        KeyPair kp = StringCypher.generateKeyPair();
        String plain = "this is a test";
        String cypherText = StringCypher.encrypt(plain, kp.getPublic());
        String plain2 = "this is a test ";
        String cypherText2 = StringCypher.encrypt(plain2, kp.getPublic());
        Assert.assertNotSame(cypherText2, cypherText);
    }

    @Test
    public void simplyCryptDecryptWithStringKeys() throws GeneralSecurityException{
        KeyPair kp = StringCypher.generateKeyPair();
        String privateKey = StringCypher.getPrivateKeyString(kp.getPrivate());
        String publicKey = StringCypher.getPublicKeyString(kp.getPublic());
        String plain = "this is a test";
        String cypherText = StringCypher.encrypt(plain,publicKey);
        String deCypherText = StringCypher.decrypt(cypherText, privateKey);
        Assert.assertEquals(plain, deCypherText);
    }
}
