package it.unimi.ssri.smc.android;

import com.peak.salut.SalutDevice;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import it.unimi.ssri.smc.network.packets.DeviceSecurityInfoPacket;

@EBean(scope = EBean.Scope.Singleton)
public class TransportData {
    private List<DeviceSecurityInfoPacket> securityInfoPacketList = new ArrayList<>();
    private List<SalutDevice> deviceList;

    public List<DeviceSecurityInfoPacket> getSecurityInfoPacketList() {
        return securityInfoPacketList;
    }

    public void setSecurityInfoPacketList(List<DeviceSecurityInfoPacket> securityInfoPacketList) {
        this.securityInfoPacketList = securityInfoPacketList;
    }

    public List<SalutDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<SalutDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public boolean allDeviceHasSecurityInfo(){
        for(DeviceSecurityInfoPacket dsip : securityInfoPacketList){
            if(!deviceList.contains(dsip.getDevice())){
                return false;
            }
        }
        return true;
    }

    public String getDevicePublicKey(SalutDevice salutDevice){
        for(DeviceSecurityInfoPacket dsip : securityInfoPacketList){
            if(dsip.getDevice().equals(salutDevice)){
                return dsip.getPublicKey();
            }
        }
        return null;
    }
}
