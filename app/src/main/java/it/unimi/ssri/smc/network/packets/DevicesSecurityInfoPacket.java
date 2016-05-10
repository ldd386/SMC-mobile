package it.unimi.ssri.smc.network.packets;


import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

@JsonObject
public class DevicesSecurityInfoPacket {

    @JsonField
    private List<DeviceSecurityInfoPacket> devicesSecurityPacket;

    public List<DeviceSecurityInfoPacket> getDevicesSecurityPacket() {
        return devicesSecurityPacket;
    }

    public void setDevicesSecurityPacket(List<DeviceSecurityInfoPacket> devicesSecurityPacket) {
        this.devicesSecurityPacket = devicesSecurityPacket;
    }
}
