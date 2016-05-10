package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS_AND_ACCESSORS)
public class DeviceSecurityInfoPacket {

    private SalutDevice device;

    private String publicKey;

    public SalutDevice getDevice() {
        return device;
    }

    public void setDevice(SalutDevice salutDevice) {
        this.device = salutDevice;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceSecurityInfoPacket)) return false;

        DeviceSecurityInfoPacket that = (DeviceSecurityInfoPacket) o;

        return !(device != null ? !device.equals(that.device) : that.device != null);

    }

    @Override
    public int hashCode() {
        return device != null ? device.hashCode() : 0;
    }
}
