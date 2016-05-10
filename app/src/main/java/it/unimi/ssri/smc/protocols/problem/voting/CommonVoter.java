package it.unimi.ssri.smc.protocols.problem.voting;

import java.math.BigInteger;
import java.util.List;

import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.DefaultUser;

public class CommonVoter implements DefaultUser{

	private int my_vote = -1;
	
	public CommonVoter(int my_vote) {
		super();
		this.my_vote = my_vote;
	}

	@Override
	public SMCProtocolPacket generateNextPacket(SMCProtocolPacket packetReceived){
		if(packetReceived.getPhaseint() == 0){
			throw new IllegalArgumentException("Wrong phase for second user!");
		} else {
			SMCProtocolPacket packetToSend = new SMCProtocolPacket();
			packetToSend.getValues().addAll(packetReceived.getValues());
			BigInteger newValueForIndex = 
					packetReceived.getValues().get(my_vote).add(BigInteger.ONE);
			packetToSend.getValues().set(my_vote, newValueForIndex);
			packetToSend.setPhaseint(packetReceived.getPhaseint()+1);
			return packetToSend;
		}
	}

	@Override
	public boolean hasNextPacket(SMCProtocolPacket packetReceived) {
		return !packetReceived.isLastPacket();
	}

    @Override
    public String getVerboseResult(SMCProtocolPacket packetWithResult) {
        StringBuilder sb = new StringBuilder("Vote results:\n");
        List<BigInteger> voteResult = readVotingResult(packetWithResult);
        for(int i = 0; i< voteResult.size();i++){
            sb.append("Vote for candidate [" + i + "] is " + voteResult.get(i) + ".\n");
        }
        return sb.toString();
    }


    public List<BigInteger> readVotingResult(SMCProtocolPacket lastPacket){
        return lastPacket.getValues();
    }

    @Override
    public boolean isSecured(){
        return false;
    }

    public int getMyVote() {
        return my_vote;
    }
}
