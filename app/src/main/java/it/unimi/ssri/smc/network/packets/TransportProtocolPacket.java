package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class TransportProtocolPacket {

    @JsonField
    private boolean isEncrypted;

    // this is the SMCProtocolPacket serialized, some time it can be encrypted with pubkey of device
    @JsonField
    private String serializedSMCProtocolPacket;

    public boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(boolean isCrypted) {
        this.isEncrypted = isCrypted;
    }

    public String getSerializedSMCProtocolPacket() {
        return serializedSMCProtocolPacket;
    }

    public void setSerializedSMCProtocolPacket(String smcProtocolPacket) {
        this.serializedSMCProtocolPacket = smcProtocolPacket;
    }
}
