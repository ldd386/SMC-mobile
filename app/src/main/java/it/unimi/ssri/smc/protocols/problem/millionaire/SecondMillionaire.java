package it.unimi.ssri.smc.protocols.problem.millionaire;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.ssri.smc.protocols.RSAIntegers;
import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.DefaultUser;

public class SecondMillionaire implements DefaultUser{

	// Bob random bit prime
	private BigInteger p;
	// Riches $$
	private BigInteger r;
	// B keys
	private BigInteger D_b;
	private BigInteger mod_b;
	// dim_u
	private int dim_u = 100;

	public SecondMillionaire(BigInteger r) {
        if(r.intValue() > dim_u)
            throw new IllegalArgumentException("Richness must be < of the max value (" + dim_u + ")" );
		this.r = r;
	}

    public SecondMillionaire(BigInteger r, int dim_u) {
        if(r.intValue() > dim_u)
            throw new IllegalArgumentException("Richness must be < of the max value (" + dim_u + ")" );
        this.r = r;
        this.dim_u = dim_u;
    }

	@Override
	public SMCProtocolPacket generateNextPacket(SMCProtocolPacket packetReceived){
		switch (packetReceived.getPhaseint()) {
			case 0:
				throw new IllegalArgumentException("0 is a Wrong phase for init user!");
			case 1:
				return generatePhase2Packet( packetReceived);
			case 2:
				throw new IllegalArgumentException("2 is a Wrong phase for secondary user!");
			default:
				throw new IllegalArgumentException("Wrong phase ( "+ packetReceived.getPhaseint() +" )for this protocol!");
		}
	}

	@Override
	public boolean hasNextPacket(SMCProtocolPacket packetReceived) {
		return !packetReceived.isLastPacket();
	}

    @Override
    public String getVerboseResult(SMCProtocolPacket packetWithResult) {
        return imRicher(packetWithResult) ? "I'm richer" : "I'm poorest";
    }

    @Override
    public boolean isSecured(){
        return true;
    }

	/*
	 * generate phase 2 packet
	 */

	private SMCProtocolPacket generatePhase2Packet(SMCProtocolPacket phase1Packet){
        if( r == null || D_b == null || mod_b == null) {
            throw new IllegalArgumentException("r or D_b od mod_b is null");
        }
		if(phase1Packet.getValues().size() != 1){
            throw new IllegalArgumentException("phase1Packet.getValues().size() != 1");
        }
		BigInteger val_a = phase1Packet.getValues().get(0);
		RSAIntegers rsa = new RSAIntegers();
		rsa.setPrivateKey(mod_b, D_b);
		List<BigInteger> y_u_array = new ArrayList<BigInteger>();
		for(int u = 1; u <= dim_u; u++){
			BigInteger y_u = rsa.decrypt(val_a.add(new BigInteger(""+u)));
			y_u_array.add(y_u);
		}
		List<BigInteger> z_u_array = generateZ_u(y_u_array);
		SMCProtocolPacket phase2packet = new SMCProtocolPacket();
		phase2packet.setPhaseint(2);
		for(int u = 0; u< z_u_array.size();u++){
			BigInteger z_u = z_u_array.get(u);
			if(r.compareTo(BigInteger.valueOf(u)) >0){
				phase2packet.getValues().add(z_u);
			}else {
				phase2packet.getValues().add(z_u.add(BigInteger.ONE));
			}
		}
		phase2packet.getValues().add(p);
		return phase2packet;
		
	}

	private List<BigInteger> generateZ_u(List<BigInteger> y_u_array) {
        List<BigInteger> z_u_array = new ArrayList<BigInteger>();
        while(z_u_array.isEmpty()){
            if(p == null) {
                p = BigInteger.probablePrime(128, new Random());
            }
            for(int u = 0; u < y_u_array.size(); u++){
                BigInteger z_u = y_u_array.get(u).mod(p);
                z_u_array.add(z_u);
            }
            if(!isGoodPrime(z_u_array)){
                p = null;
                z_u_array.clear();
            }
        }
        return z_u_array;
	}


	private boolean isGoodPrime(List<BigInteger> z_u_array) {
		if(z_u_array.isEmpty()) return false;
		for(int u = 0; u < z_u_array.size(); u++){
			for(int v = 0; v < z_u_array.size(); v++){
				if(u == v) continue;			
				BigInteger subresult = z_u_array.get(u).subtract(z_u_array.get(v)).abs();
				if(subresult.compareTo(new BigInteger("2")) <0){
					return false;
				}
			}
		}
		for(int u = 0; u < z_u_array.size(); u++){
			BigInteger z_u = z_u_array.get(u);
			if(z_u.compareTo(BigInteger.ZERO) <0 || 
					z_u.compareTo(p.subtract(BigInteger.ONE)) >=0){
				return false;
			}
		}
		return true;
	}
	

	public boolean imRicher(SMCProtocolPacket phase3Packet){
		if(phase3Packet.getValues().size() != 1){
            throw new IllegalArgumentException("phase3Packet.getValues().size() != 1");
        }
		return
			!phase3Packet.getValues().get(0).equals(BigInteger.ZERO);

	}

	public void setP(BigInteger p){
		this.p = p;
	}
	public BigInteger getP(){
		return p;
	}
	
	public void setB_privkey(BigInteger D_b, BigInteger mod_b) {
		this.D_b = D_b;
		this.mod_b = mod_b;
	}

}
