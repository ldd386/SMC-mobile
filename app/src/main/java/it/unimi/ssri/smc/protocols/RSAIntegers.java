package it.unimi.ssri.smc.protocols;

import java.math.BigInteger;
import java.util.Random;


public class RSAIntegers {
	
	private BigInteger d;
	private BigInteger e;
	private BigInteger modulus;

	private final static Random random = new Random();

	public RSAIntegers() {
		
	}
	
	/** Create an instance that can encrypt using someone else public key. */
	public RSAIntegers(BigInteger modulus, BigInteger e) {
		this.modulus = modulus;
		this.e = e;
	}

	/** generate an N-bit public and private key */
	public void generatesKeys(int N) {
		BigInteger p = BigInteger.probablePrime(N / 2, random);
		BigInteger q = BigInteger.probablePrime(N / 2, random);
		BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q
				.subtract(BigInteger.ONE)); // is m
		modulus = p.multiply(q);
		// common value in practice 65537 = 2^16 +1, to avoid the while cycle
		e = new BigInteger("3"); 
		while (phi.gcd(e).intValue() > 1) {
			e = e.add(new BigInteger("2"));
		}
		d = e.modInverse(phi);
	}

	public BigInteger encrypt(BigInteger message) {
		return message.modPow(e, modulus);
	}

	public BigInteger decrypt(BigInteger encrypted) {
		return encrypted.modPow(d, modulus);
	}

	/** Return the modulus. */
	public BigInteger getModulus() {
		return modulus;
	}

	/** Return the public key. */
	public BigInteger getE() {
		return e;
	}

	/** Return the private key. */
	public BigInteger getD() {
		return d;
	}

	/** set the private key **/
	public void setPrivateKey(BigInteger modulus, BigInteger d){
		this.modulus = modulus;
		this.d = d;
	}
}