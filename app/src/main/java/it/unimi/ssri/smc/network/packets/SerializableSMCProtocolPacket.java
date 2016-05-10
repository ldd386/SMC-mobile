package it.unimi.ssri.smc.network.packets;


import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import it.unimi.ssri.smc.network.converter.BigIntegerListConverter;
import it.unimi.ssri.smc.protocols.SMCProtocolPacket;

@JsonObject
public class SerializableSMCProtocolPacket {

    @JsonField
    private int phaseint = 0;

    @JsonField
    private boolean lastPacket = false;

    @JsonField(typeConverter = BigIntegerListConverter.class)
    private List<BigInteger> values = new ArrayList<BigInteger>();

    public SerializableSMCProtocolPacket(){}

    public SerializableSMCProtocolPacket(SMCProtocolPacket smcProtocolPacket){
        phaseint = smcProtocolPacket.getPhaseint();
        lastPacket = smcProtocolPacket.isLastPacket();
        values = smcProtocolPacket.getValues();
    }

    public SMCProtocolPacket getPacket(){
        SMCProtocolPacket smcProtocolPacket = new SMCProtocolPacket();
        smcProtocolPacket.setLastPacket(lastPacket);
        smcProtocolPacket.setValues(values);
        smcProtocolPacket.setPhaseint(phaseint);
        return smcProtocolPacket;
    }

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
