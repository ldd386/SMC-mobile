package it.unimi.ssri.smc.network.packets;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class VotingExtraDataPacket {

    @JsonField
    private int numberOfCandidates;

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public void setNumberOfCandidates(int numberOfCandidates) {
        this.numberOfCandidates = numberOfCandidates;
    }

}
