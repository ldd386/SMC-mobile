package it.unimi.ssri.smc.protocols.problem.voting;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.InitUser;

public class InitVoter implements InitUser{

	private static final int MAX_CANDIDATES = 10;
	private static final int MAX_VOTERS = 100;
	
	private int n_voters;
	private List<BigInteger> candidates_vote = new ArrayList<>();
	private int my_vote = -1;
	
	private SMCProtocolPacket lastPacket = null;
	
	public InitVoter(int n_candidates, int n_voters, int my_vote){
		if(n_candidates > MAX_CANDIDATES){
            throw new IllegalArgumentException("n of candidates must be less than "
                    + MAX_CANDIDATES + " but is " + n_candidates);
        }
        if(n_voters > MAX_VOTERS || n_voters < 3){
            throw new IllegalArgumentException("n of voters must be less than "
                    + MAX_CANDIDATES + " and greater than 3 but is " + n_candidates);
        }
		this.n_voters = n_voters;
		this.my_vote = my_vote;
		for(int i = 0; i< n_candidates; i++){
			candidates_vote.add(BigInteger.probablePrime(10, new Random()));
		}
	}

    @Override
    public int getGroupSize() {
        return n_voters;
    }

    @Override
    public boolean isSecured(){
        return false;
    }

    @Override
	public SMCProtocolPacket generateNextPacket(SMCProtocolPacket packetReceived){
		if(packetReceived.getPhaseint() == 0){
			throw new IllegalArgumentException(
					"Please call generateInitializationPacket() for this phase");
		} else if(packetReceived.getPhaseint() == n_voters){
			return generateLastPacket(packetReceived);
		} else {
			throw new IllegalArgumentException("Wrong phase for init user, found: "
					+packetReceived.getPhaseint() + " expected: " + (n_voters-1));
		}
		
	}
	private SMCProtocolPacket generateLastPacket(SMCProtocolPacket lastPacket){
		List<BigInteger> newcandidates_vote = new ArrayList<>();
		newcandidates_vote.addAll(lastPacket.getValues());
		if(newcandidates_vote.size() != candidates_vote.size()){
            throw new IllegalArgumentException("Wrong size for candidate vote");
        }
		// adding my vote
        if(my_vote == -1){
            throw new IllegalArgumentException("Wrong vote for myself, it is -1!");
        }
		BigInteger newValueForIndex =
				newcandidates_vote.get(my_vote).add(BigInteger.ONE);
		newcandidates_vote.set(my_vote, newValueForIndex);

		SMCProtocolPacket smcProtocolPacket = new SMCProtocolPacket();
		for(int i= 0; i< newcandidates_vote.size(); i++){
			BigInteger finalVoteForPosition = 
					newcandidates_vote.get(i).subtract(candidates_vote.get(i));
			smcProtocolPacket.getValues().add(finalVoteForPosition);
		}
		// check for irregularity
		BigInteger c = BigInteger.ZERO;
		for(BigInteger bi : smcProtocolPacket.getValues()){
			c = c.add(bi);
		}
		if(c.intValue() != n_voters ){
			throw new IllegalArgumentException("Wrong number of voters: " 
									+ c.intValue()  + " expected " + n_voters);
		}
		lastPacket = smcProtocolPacket;
		smcProtocolPacket.setLastPacket(true);
		return smcProtocolPacket;
	}

	@Override
	public boolean hasNextPacket(SMCProtocolPacket packetReceived) {
		return lastPacket != null;
	}

    @Override
    public String getVerboseResult(SMCProtocolPacket packetWithResult) {
        StringBuilder sb = new StringBuilder("Vote results:\n");
        List<BigInteger> voteResult = readVotingResult(packetWithResult);
        for(int i = 0; i< voteResult.size();i++){
            sb.append("Vote for candidate [" + i + "] is " + voteResult.get(i) + "\n");
        }
        return sb.toString();
    }

    public List<BigInteger> readVotingResult(SMCProtocolPacket lastPacket){
        return lastPacket.getValues();
    }

	/*
	 * generate initialization packet
	 */
	@Override
	public SMCProtocolPacket generateInitializationPacket(){
		SMCProtocolPacket smcProtocolPacket = new SMCProtocolPacket();
		smcProtocolPacket.setPhaseint(1);
		smcProtocolPacket.setValues(candidates_vote);
		return smcProtocolPacket;
	}

	@Override
	public SMCProtocolPacket getResponsePacket() {
		return lastPacket;
	}

    public int getNVoters() {
        return n_voters;
    }
}
