package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class ConfirmationPacket {

    @JsonField
    private String typeOfPacketToConfirm;

    public String getTypeOfPacketToConfirm() {
        return typeOfPacketToConfirm;
    }

    public void setTypeOfPacketToConfirm(String typeOfPacketToConfirm) {
        this.typeOfPacketToConfirm = typeOfPacketToConfirm;
    }

}
