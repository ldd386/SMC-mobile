package it.unimi.ssri.smc.android;

import org.androidannotations.annotations.EBean;

import java.security.KeyPair;

import it.unimi.ssri.smc.protocols.problem.DefaultUser;

@EBean(scope = EBean.Scope.Singleton)
public class UserData {

    private KeyPair keyPair;

    private boolean isInitUser;
    private String protocolName;

    private String deviceName;
    private DefaultUser defaultUser;

    public DefaultUser getDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(DefaultUser defaultUser) {
        this.defaultUser = defaultUser;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isInitUser() {
        return isInitUser;
    }

    public void setIsInitUser(boolean isInitUser) {
        this.isInitUser = isInitUser;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
