package it.unimi.ssri.smc.protocols.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.voting.CommonVoter;
import it.unimi.ssri.smc.protocols.problem.voting.InitVoter;

@RunWith(JUnit4.class)
public class VotingProblemTest {
	@Test
	public  void initIsCorrect() {
		InitVoter mp = new InitVoter(2,5,1);
		SMCProtocolPacket packet = mp.generateInitializationPacket();
		Assert.assertEquals(1,packet.getPhaseint());
		Assert.assertEquals(2,packet.getValues().size());
	}
	
	@Test
	public void phase2And3Correct() {
		InitVoter mp = new InitVoter(2,5,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		Assert.assertEquals(2,packet1.getPhaseint());
		Assert.assertEquals(2,packet1.getValues().size());
		
		CommonVoter su2 = new CommonVoter(1);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		Assert.assertEquals(3,packet2.getPhaseint());
		Assert.assertEquals(2,packet2.getValues().size());
	}
	
	@Test(expected = IllegalArgumentException.class)  
	public void phase4IsTooSoon() {
		InitVoter mp = new InitVoter(2,5,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		Assert.assertEquals(2,packet1.getPhaseint());
		Assert.assertEquals(2,packet1.getValues().size());
		
		CommonVoter su2 = new CommonVoter(1);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		Assert.assertEquals(3,packet2.getPhaseint());
		Assert.assertEquals(2,packet2.getValues().size());
		
		mp.generateNextPacket(packet2);
	}
	@Test
	public void endingPhase3IsOk() {
		InitVoter mp = new InitVoter(2,3,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		Assert.assertEquals(2,packet1.getPhaseint());
		Assert.assertEquals(2,packet1.getValues().size());
		
		CommonVoter su2 = new CommonVoter(1);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		Assert.assertEquals(3,packet2.getPhaseint());
		Assert.assertEquals(2,packet2.getValues().size());
		
		SMCProtocolPacket packetEnd = mp.generateNextPacket(packet2);
		Assert.assertEquals(0,packetEnd.getValues().get(0).intValue());
		Assert.assertEquals(3,packetEnd.getValues().get(1).intValue());
	}
	
	@Test(expected = IllegalArgumentException.class)  
	public void endingPhaseWithManipulationFail() {
		InitVoter mp = new InitVoter(2,3,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		
		CommonVoter su2 = new CommonVoter(0);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		packet2.getValues().set(1,BigInteger.TEN);
		mp.generateNextPacket(packet2);
	}
	
	@Test(expected = IllegalArgumentException.class)  
	public void endingPhaseWithManipulationFail2() {
		InitVoter mp = new InitVoter(2,3,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		
		CommonVoter su2 = new CommonVoter(0);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		packet2.getValues().set(1,BigInteger.ZERO);
		mp.generateNextPacket(packet2);
	}
	
	@Test
	public void endingPhaseWithCleverManipulationCanNotFail() {
		InitVoter mp = new InitVoter(2,3,1);
		SMCProtocolPacket packet0 = mp.generateInitializationPacket();
		// first user
		CommonVoter su1 = new CommonVoter(1);
		SMCProtocolPacket packet1 = su1.generateNextPacket(packet0);
		// second user
		CommonVoter su2 = new CommonVoter(1);
		SMCProtocolPacket packet2 = su2.generateNextPacket(packet1);
		//doing manipulation
		packet2.getValues().set(0, 
				packet2.getValues().get(0).add(new BigInteger("2")));
		packet2.getValues().set(1, 
				packet2.getValues().get(1).subtract(new BigInteger("2")));
		mp.generateNextPacket(packet2);
	}
}
