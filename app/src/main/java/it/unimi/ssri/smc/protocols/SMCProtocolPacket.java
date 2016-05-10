package it.unimi.ssri.smc.protocols;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SMCProtocolPacket {

	private int phaseint = 0;
	private boolean lastPacket = false;

	private List<BigInteger> values = new ArrayList<BigInteger>();
	
	public int getPhaseint() {
		return phaseint;
	}

	public void setPhaseint(int phaseint) {
		this.phaseint = phaseint;
	}

	public List<BigInteger> getValues() {
		return values;
	}

	public void setValues(List<BigInteger> values) {
		this.values = values;
	}

	public boolean isLastPacket(){
		return lastPacket;
	}

	public void setLastPacket(boolean lastPacket){
		this.lastPacket = lastPacket;
	}
}
