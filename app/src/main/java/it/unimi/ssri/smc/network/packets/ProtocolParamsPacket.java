package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

import java.util.List;

@JsonObject
public class ProtocolParamsPacket {

    @JsonField
    private List<SalutDevice> devicesInComputation;

    @JsonField
    private boolean isEncryptionOn;

    @JsonField
    private String otherSerializedData; // other data related to protocol.

    public List<SalutDevice> getDevicesInComputation() {
        return devicesInComputation;
    }

    public void setDevicesInComputation(List<SalutDevice> devicesInComputation) {
        this.devicesInComputation = devicesInComputation;
    }

    public String getOtherSerializedData() {
        return otherSerializedData;
    }

    public void setOtherSerializedData(String otherSerializedData) {
        this.otherSerializedData = otherSerializedData;
    }

    public boolean getIsEncryptionOn() {
        return isEncryptionOn;
    }

    public void setIsEncryptionOn(boolean isEncryptionOn) {
        this.isEncryptionOn = isEncryptionOn;
    }
}
