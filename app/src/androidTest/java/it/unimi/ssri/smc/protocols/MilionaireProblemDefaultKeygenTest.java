package it.unimi.ssri.smc.protocols;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import it.unimi.ssri.smc.network.StringCypher;
import it.unimi.ssri.smc.protocols.problem.millionaire.InitMillionaire;
import it.unimi.ssri.smc.protocols.problem.millionaire.SecondMillionaire;

@RunWith(JUnit4.class)
public class MilionaireProblemDefaultKeygenTest {

    @Test
    public void firstRicher() throws GeneralSecurityException{
        KeyPair kp = StringCypher.generateKeyPair();
        RSAPrivateKey rsaPriv =
                (RSAPrivateKey) kp.getPrivate();
        RSAPublicKey rsaPub =
                (RSAPublicKey) kp.getPublic();

        InitMillionaire mp_a = new InitMillionaire(new BigInteger("49"));
        mp_a.setB_pubkey(rsaPub.getPublicExponent(), rsaPub.getModulus());
        SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
        Assert.assertFalse("packet 1 is not last packet", packet1.isLastPacket());



        SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("7"));
        mp_b.setB_privkey(rsaPriv.getPrivateExponent(), rsaPub.getModulus());
        SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
        Assert.assertFalse("packet 2 is not last packet", packet2.isLastPacket());

        SMCProtocolPacket packet3 = mp_a.generateNextPacket(packet2);

        Assert.assertEquals("init user is richer", BigInteger.ZERO, packet3.getValues().get(0));
        Assert.assertTrue("init user is richer", mp_a.imRicher());
        Assert.assertFalse("second user is not richer", mp_b.imRicher(packet3));
        Assert.assertTrue("packet 3 is last packet", packet3.isLastPacket());
    }

    @Test
    public void secondRicher() throws GeneralSecurityException{
        KeyPair kp = StringCypher.generateKeyPair();
        RSAPrivateKey rsaPriv =
                (RSAPrivateKey) kp.getPrivate();
        RSAPublicKey rsaPub =
                (RSAPublicKey) kp.getPublic();

        InitMillionaire mp_a = new InitMillionaire(new BigInteger("1"));
        mp_a.setB_pubkey(rsaPub.getPublicExponent(), rsaPriv.getModulus());
        SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
        Assert.assertFalse("packet 1 is not last packet", packet1.isLastPacket());


        SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("51"));
        mp_b.setB_privkey(rsaPriv.getPrivateExponent(), rsaPub.getModulus());
        SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
        Assert.assertFalse("packet 2 is not last packet", packet2.isLastPacket());

        SMCProtocolPacket packet3 = mp_a.generateNextPacket(packet2);
        Assert.assertTrue("second user is richer", mp_b.imRicher(packet3));
        Assert.assertTrue("packet 3 is last packet", packet3.isLastPacket());
    }
}
