package it.unimi.ssri.smc.protocols.problem;

import it.unimi.ssri.smc.protocols.SMCProtocolPacket;

public interface DefaultUser {
    boolean isSecured(); //todo
	SMCProtocolPacket generateNextPacket(SMCProtocolPacket packetReceived);
	boolean hasNextPacket(SMCProtocolPacket packetReceived);
    String getVerboseResult(SMCProtocolPacket packetWithResult);
}
