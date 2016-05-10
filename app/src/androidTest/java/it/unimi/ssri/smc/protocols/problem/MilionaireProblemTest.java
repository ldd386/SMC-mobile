package it.unimi.ssri.smc.protocols.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unimi.ssri.smc.protocols.RSAIntegers;
import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.millionaire.InitMillionaire;
import it.unimi.ssri.smc.protocols.problem.millionaire.SecondMillionaire;

@RunWith(JUnit4.class)
public class MilionaireProblemTest {

	@Test
	public  void initIsCorrect() {
		InitMillionaire mp = new InitMillionaire(new BigInteger("4"));
		mp.setB_pubkey(new BigInteger("7"), new BigInteger("55"));
		mp.setX(new BigInteger("39"));
		SMCProtocolPacket packet = mp.generateInitializationPacket();
		Assert.assertEquals("the packet is in the first phase", 1,packet.getPhaseint());
		Assert.assertEquals("in the first phase values is a ",1,packet.getValues().size());
		Assert.assertEquals(new BigInteger("15"), packet.getValues().get(0));
	}
	
	@Test
	public  void phase2isCorrect() {
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("4"), 4);
		mp_a.setB_pubkey(new BigInteger("7"), new BigInteger("55"));
		mp_a.setX(new BigInteger("39"));
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("2"), 4);
		mp_b.setB_privkey(new BigInteger("23"), new BigInteger("55"));
		mp_b.setP(new BigInteger("31"));
		SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
		Assert.assertEquals(2, packet2.getPhaseint());
		
		List<Integer> valuesInt = new ArrayList<>();
		for(BigInteger biginteger : packet2.getValues()){
			valuesInt.add(biginteger.intValue());
		}
		Assert.assertEquals(Arrays.asList(26,18,3,9,31), valuesInt);
	}
	
	//@Test
	public  void phase2isNotCorrect() {
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("44"), 60);
		mp_a.setB_pubkey(new BigInteger("7"), new BigInteger("55"));
		mp_a.setX(new BigInteger("39"));
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("52"), 60);
		mp_b.setB_privkey(new BigInteger("23"), new BigInteger("55"));
		BigInteger p = new BigInteger("11");
		mp_b.setP(p); // not used, is coprime with 44
		mp_b.generateNextPacket(packet1);
		Assert.assertFalse("the chosen p (11) is coprime with the Alice R_c (44), must be changed.",
							p.equals(mp_b.getP()));
	}
	
	@Test
	public  void phase3isCorrect() {
		InitMillionaire mp = new InitMillionaire(new BigInteger("4"),4);
		mp.setB_pubkey(new BigInteger("7"), new BigInteger("55"));
		mp.setX(new BigInteger("39"));
		SMCProtocolPacket packet2 = new SMCProtocolPacket();
		packet2.setPhaseint(2);
		List<BigInteger> valuesBigInt = new ArrayList<>();
		valuesBigInt.add(new BigInteger("26"));
		valuesBigInt.add(new BigInteger("18"));
		valuesBigInt.add(new BigInteger("3"));
		valuesBigInt.add(new BigInteger("9"));
		valuesBigInt.add(new BigInteger("31"));
		packet2.setValues(valuesBigInt);
		SMCProtocolPacket packet3 = mp.generateNextPacket(packet2);
		Assert.assertEquals("first user win with custom values in packet",
				BigInteger.ZERO, packet3.getValues().get(0));
	}
	
	
	@Test
	public  void phase3isCorrectLong() {
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("4"), 4);
		mp_a.setB_pubkey(new BigInteger("7"), new BigInteger("55"));
		mp_a.setX(new BigInteger("39"));
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("2"),4);
		mp_b.setB_privkey(new BigInteger("23"), new BigInteger("55"));
		mp_b.setP(new BigInteger("31"));
		SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
		
		SMCProtocolPacket packet3 = mp_a.generateNextPacket(packet2);
		Assert.assertEquals("first user is richer",
				BigInteger.ZERO, packet3.getValues().get(0));
	}
	
	@Test
	public void phase3isCorrectLong2() {
		RSAIntegers rsaInt = new RSAIntegers();
		rsaInt.generatesKeys(1000);
		
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("4"));
		mp_a.setB_pubkey(rsaInt.getE(), rsaInt.getModulus());
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("22"));
		mp_b.setB_privkey(rsaInt.getD(), rsaInt.getModulus());
		SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
		
		SMCProtocolPacket packet3 = mp_a.generateNextPacket(packet2);
		Assert.assertEquals("second user is richer",
				BigInteger.ONE, packet3.getValues().get(0));
	}
	
	@Test
	public void phase3isCorrectLong3() {
		RSAIntegers rsaInt = new RSAIntegers();
		rsaInt.generatesKeys(1000);
		
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("49"));
		mp_a.setB_pubkey(rsaInt.getE(), rsaInt.getModulus());
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("7"));
		mp_b.setB_privkey(rsaInt.getD(), rsaInt.getModulus());
		SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
		
		SMCProtocolPacket packet3 = mp_a.generateNextPacket(packet2);
		Assert.assertEquals("init user is richer", BigInteger.ZERO, packet3.getValues().get(0));
	}
	
	//@Test(expected=IndexOutOfBoundsException.class)
	public void phase3FailWithShortDimU() {
		RSAIntegers rsaInt = new RSAIntegers();
		rsaInt.generatesKeys(100);
		
		InitMillionaire mp_a = new InitMillionaire(new BigInteger("4"),500);
		mp_a.setB_pubkey(rsaInt.getE(), rsaInt.getModulus());
		//mp_a.setDim_u(500);
		SMCProtocolPacket packet1 = mp_a.generateInitializationPacket();
		
		SecondMillionaire mp_b = new SecondMillionaire(new BigInteger("7"),500);
		mp_b.setB_privkey(rsaInt.getD(), rsaInt.getModulus());
		//mp_b.setDim_u(500);
		SMCProtocolPacket packet2 = mp_b.generateNextPacket(packet1);
		mp_a.generateNextPacket(packet2);
	}
}
