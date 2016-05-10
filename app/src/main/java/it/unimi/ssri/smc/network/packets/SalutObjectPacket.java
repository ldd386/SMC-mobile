package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

import java.io.IOException;

@JsonObject
public class SalutObjectPacket {

    @JsonField
    private SalutDevice senderDevice;

    @JsonField
    private SalutDevice receiverDevice; // can be null if host

    @JsonField
    protected String typeOfPacket;

    @JsonField
    protected String serializedPacket;

    public SalutObjectPacket(){
        // required by LoganSquare
    }

    public SalutObjectPacket(Object o) throws IOException{
        typeOfPacket = o.getClass().getName();
        serializedPacket = LoganSquare.serialize(o);
    }

    public SalutObjectPacket(Object o, SalutDevice senderDevice) throws IOException{
        this.senderDevice = senderDevice;
        typeOfPacket = o.getClass().getName();
        serializedPacket = LoganSquare.serialize(o);
    }

    public Object getPacket() throws IOException, ClassNotFoundException {
        Class c = Class.forName(typeOfPacket);
        return LoganSquare.parse(serializedPacket,c);
    }

    public <E> E getPacket(Class<E> classOfPacket) throws IOException {
        return LoganSquare.parse(serializedPacket, classOfPacket);
    }

    public String getTypeOfPacket() {
        return typeOfPacket;
    }

    public SalutDevice getSenderDevice() {
        return senderDevice;
    }

    public void setSenderDevice(SalutDevice senderDevice) {
        this.senderDevice = senderDevice;
    }

    public SalutDevice getReceiverDevice() {
        return receiverDevice;
    }

    public void setReceiverDevice(SalutDevice receiverDevice) {
        this.receiverDevice = receiverDevice;
    }

    public boolean isPacketOfType(Class c){
        return c.getName().equals(typeOfPacket);
    }
}
