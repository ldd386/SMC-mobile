package it.unimi.ssri.smc.protocols.problem;

import it.unimi.ssri.smc.protocols.SMCProtocolPacket;

public interface InitUser extends DefaultUser{
    int getGroupSize();
	SMCProtocolPacket generateInitializationPacket();
	SMCProtocolPacket getResponsePacket();
}
