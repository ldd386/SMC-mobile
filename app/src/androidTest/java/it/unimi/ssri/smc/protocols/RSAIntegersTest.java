package it.unimi.ssri.smc.protocols;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public class RSAIntegersTest {

	@Test
	public void encDecTest() {
		RSAIntegers key = new RSAIntegers();
		key.generatesKeys(1000);
		System.out.println(key);

		BigInteger x1 = new BigInteger("1000");

		BigInteger enc_x1 = key.encrypt(x1);

		BigInteger dec_enc_x1 = key.decrypt(enc_x1);
		Assert.assertEquals(dec_enc_x1, x1);
	}
	
	@Test
	public void rsaTextToIntegerTest() {
		RSAIntegers key = new RSAIntegers();
		key.generatesKeys(1000);
		BigInteger x1 = new BigInteger("ciao amici miei".getBytes());
		BigInteger enc_x1 = key.encrypt(x1);
		BigInteger dec_h = key.decrypt(enc_x1);
		Assert.assertEquals(new String(dec_h.toByteArray()), "ciao amici miei");
	}
	
	/**
	 * If the RSA public key is modulus m and exponent e, 
	 * then the encryption of a message x is given by ϵ(x)=x^e mod m. </br>
	 * The homomorphic property is then</br>
	 * ϵ(x1)*ϵ(x2) = x1^e*x2^e mod m = (x1*x2)^e mod m = ϵ(x1*x2)
	 */
	@Test
	public void omomorphismTest() {
		RSAIntegers key = new RSAIntegers();
		key.generatesKeys(1000);

		BigInteger x1 = new BigInteger("100");
		BigInteger x2 = new BigInteger("2");

		BigInteger enc_x1 = key.encrypt(x1);
		BigInteger enc_x2 = key.encrypt(x2);

		BigInteger homomorphic = enc_x1.multiply(enc_x2);
		BigInteger dec_h = key.decrypt(homomorphic);
		Assert.assertEquals(dec_h.intValue(), 200);
	}

}
