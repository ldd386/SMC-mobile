package it.unimi.ssri.smc.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import it.unimi.ssri.smc.network.StringCypher;
import it.unimi.ssri.smc.network.packets.ConfirmationPacket;
import it.unimi.ssri.smc.network.packets.DeviceSecurityInfoPacket;
import it.unimi.ssri.smc.network.packets.DevicesSecurityInfoPacket;
import it.unimi.ssri.smc.network.packets.FailurePacket;
import it.unimi.ssri.smc.network.packets.ProtocolParamsPacket;
import it.unimi.ssri.smc.network.packets.SalutObjectPacket;
import it.unimi.ssri.smc.network.packets.SerializableSMCProtocolPacket;
import it.unimi.ssri.smc.network.packets.TransportProtocolPacket;
import it.unimi.ssri.smc.network.packets.VotingExtraDataPacket;
import it.unimi.ssri.smc.protocols.SMCProtocolPacket;
import it.unimi.ssri.smc.protocols.problem.millionaire.SecondMillionaire;
import it.unimi.ssri.smc.protocols.problem.voting.CommonVoter;

@EActivity(R.layout.activity_single_fragment)
public class SecondaryDeviceActivity extends AppCompatActivity implements SalutDataCallback {

    private static final String TAG = "SECONDARY_DEVICE_ACTVT";

    @Bean
    protected UserData mUserData;

    @Bean
    protected TransportData mTransportData;

    private Salut network;

    private DevicesListFragment mDeviceSelectionFragment;
    private RunningProtocolFragment mProtocolFragment;

    @InstanceState
    protected int mProtocolStatus = Constants.STATUS_SYNCING;

    @AfterInject
    protected void initializeActivity(){
        addDeviceListFragment();
        asyncServiceRegistration();
    }

    @AfterViews
    protected void setViewItems(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("SMC: " + mUserData.getProtocolName() + " protocol");
    }

    protected void addDeviceListFragment() {
        mDeviceSelectionFragment = DevicesListFragment_.builder()
                .mNOfDesiredDevices(1).mDeviceListTitle("Please, select a service.").build();
        mDeviceSelectionFragment.setOnNextListener(new DevicesListFragment.NavigationListener() {
            @Override
            public void onNextClicked() {
                getFragmentManager().beginTransaction().remove(mDeviceSelectionFragment).commit();
                final SalutDevice salutDevice = mDeviceSelectionFragment.getSelected().get(0);
                network.registerWithHost(salutDevice, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.i(TAG, "We're now registered to " + salutDevice.instanceName);
                            }
                        },
                        new SalutCallback() {
                            @Override
                            public void call() {
                                if(mProtocolStatus != Constants.STATUS_FINISHED) {
                                    Log.i(TAG, "Failed to register to " + salutDevice.instanceName);
                                    notificateFailureDialog("We failed to register to " + salutDevice.instanceName);
                                }
                            }
                        });
            }
        });
        mProtocolFragment = RunningProtocolFragment_.builder().build();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mProtocolFragment).commit();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mDeviceSelectionFragment).commit();
    }

    protected void asyncServiceRegistration() {
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, this);
        SalutServiceData serviceData = new SalutServiceData(Constants.SERVICE_PREFIX + mUserData.getProtocolName(),
                Constants.SERVICE_PORT, mUserData.getDeviceName());
         network= new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "This device does not support WiFi Direct.");
                notificateFailureDialog("Sorry, but this device does not support WiFi Direct.");
            }
        });
        discoverServiceWithTimeout();

    }

    protected void discoverServiceWithTimeout() {
        if (mProtocolStatus != Constants.STATUS_SYNCING) return;
        network.discoverWithTimeout(new SalutCallback() {
            @Override
            public void call() {
                for (SalutDevice initDevice : network.foundDevices) {
                        Log.i(TAG, "An init device has been discovered with the name " + initDevice.deviceName);
                        if (!initDevice.serviceName.contains(mUserData.getProtocolName())) return;
                        mDeviceSelectionFragment.addDeviceToList(initDevice);
                }
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondaryDeviceActivity.this);
                builder.setTitle("Service not found");
                builder.setMessage("Try again?");
                builder.setCancelable(false);
                builder.setNegativeButton("Terminate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        return;
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        discoverServiceWithTimeout();
                    }
                });
                builder.create().show();
            }
        }, 5000);
    }

    @Override
    public void onDestroy() {
        network.unregisterClient(
                new SalutCallback() {
                    @Override
                    public void call() {
                        // on success
                    }
                },
                new SalutCallback() {
                    @Override
                    public void call() {
                        // on failure: do it again
                        network.unregisterClient(true);
                    }
                }, false);
            super.onDestroy();
    }

    /* ******************************************** *
     *                                              *
     *             PACKETs MANAGEMENT               *
     *                                              *
     * ******************************************** */

    @Override
    public void onDataReceived(Object o) {
        Log.i(TAG, "Data received!");
        if(mProtocolStatus == Constants.STATUS_FINISHED){
            Log.i(TAG, "Protocol finished, packed cannot be managed.");
            return;
        }
        try {
            SalutObjectPacket salutObjectPacket = LoganSquare.parse((String) o, SalutObjectPacket.class);
            Log.i(TAG, "Type of packet received is: " + salutObjectPacket.getTypeOfPacket().toString());

            if (salutObjectPacket.isPacketOfType(ProtocolParamsPacket.class)) {
                logPacket("Received ProtocolParamsPacket.");
                ProtocolParamsPacket protocolParamsPacket =
                        (ProtocolParamsPacket) salutObjectPacket.getPacket();
                mTransportData.setDeviceList(protocolParamsPacket.getDevicesInComputation());
                StringBuilder sbDevices = new StringBuilder("Devices in computation are: \n");
                for (SalutDevice device : mTransportData.getDeviceList()) {
                    sbDevices.append(device.readableName + "[" + device.deviceName + "]\n");
                }
                logPacket(sbDevices.toString());
                if (Constants.PROTOCOL_NAME_VOTING.equals(mUserData.getProtocolName())
                        && myVoteIsNotValid(protocolParamsPacket)) {
                    return;
                }
                if (protocolParamsPacket.getIsEncryptionOn()) {
                    sendSecurityInformation();
                }
            } else if (salutObjectPacket.isPacketOfType(FailurePacket.class)) {
                logPacket("Failure packet received");
                if(!network.thisDevice.equals(salutObjectPacket.getSenderDevice())) {
                    notificateFailureDialog(((FailurePacket) salutObjectPacket.getPacket()).getFailureReason());
                }
            } else if (salutObjectPacket.isPacketOfType(DevicesSecurityInfoPacket.class)) {
                DevicesSecurityInfoPacket dsip = (DevicesSecurityInfoPacket) salutObjectPacket.getPacket();
                manageDevicesSecurityInfo(dsip);
            } else if(salutObjectPacket.isPacketOfType(TransportProtocolPacket.class)){
                logPacket("Received TransportProtocolPacket with smc packet inside.");
                TransportProtocolPacket tpp = (TransportProtocolPacket) salutObjectPacket.getPacket();
                if(network.thisDevice.equals(salutObjectPacket.getReceiverDevice())) {
                    logPacket("Transport Protocol packet is for this device.");
                    manageSMCTransport(tpp);
                } else if (salutObjectPacket.getReceiverDevice() == null){
                    logPacket("Transport Protocol packet is for all device.");
                    manageSMCTransport(tpp);
                } else {
                    logPacket("Transport Protocol packet is -not- for this device.");
                }
            }
        }
        catch (IOException|ClassNotFoundException ex){
            Log.e(TAG, "Failed to parse network data.");
            notificateFailureDialog("Failed to parse network data.");
        }
    }

    //TODO: fix this: is not nice to have a side effect method
    private boolean myVoteIsNotValid(ProtocolParamsPacket protocolParamsPacket) {
        try {
            VotingExtraDataPacket votingExtraDataPacket =
                    LoganSquare.parse(protocolParamsPacket.getOtherSerializedData(), VotingExtraDataPacket.class);
            int yourVote = ((CommonVoter) mUserData.getDefaultUser()).getMyVote();
            if (yourVote >=
                    votingExtraDataPacket.getNumberOfCandidates()) {
                String reason = "Your vote " + yourVote +" is out of range ( 0 - "
                        + votingExtraDataPacket.getNumberOfCandidates() + " )";
                notificateFailureDialog(reason);
                sendFailure(mUserData.getDeviceName() + " parameters are wrong.");
                mProtocolStatus = Constants.STATUS_FINISHED;
                return true;
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Failure when reading extra data for voting protocol in protocolParamsPacket", ioe);
            notificateFailureDialog("Failure when reading extra data in protocol parameters to devices");
            return true;
        }
        return false;
    }

    private void manageSMCTransport(TransportProtocolPacket tsp) throws IOException {
        String maybeEncryptedPacket = tsp.getSerializedSMCProtocolPacket();
        try {
            String decryptedPacket = tsp.getIsEncrypted()?
                    StringCypher.decrypt(maybeEncryptedPacket,
                            mUserData.getKeyPair().getPrivate()) : maybeEncryptedPacket;
            SerializableSMCProtocolPacket serializableSMCProtocolPacket =
                    LoganSquare.parse(decryptedPacket, SerializableSMCProtocolPacket.class);
            SMCProtocolPacket smcProtocolPacket = serializableSMCProtocolPacket.getPacket();
            if(mUserData.getDefaultUser().hasNextPacket(smcProtocolPacket)){
                Log.d(TAG, "Generation of new SMC protocol packet..");
                manageNextSMCPacket(smcProtocolPacket);
            } else {
                Log.d(TAG, "Reading last SMC protocol packet..");
                mProtocolStatus = Constants.STATUS_FINISHED;
                try{
                    String verboseResult = mUserData.getDefaultUser().getVerboseResult(smcProtocolPacket);
                    logPacket(verboseResult);
                    Log.d(TAG, "No next packet. ");
                    mProtocolFragment.showLastStatus();
                }catch(IllegalArgumentException iae){
                    Log.e(TAG, "Cannot retrieving response");
                    notificateFailureDialog("Error while retrieving response");
                }
            }
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed in decryption of smc packet data.");
            notificateFailureDialog("Failed in decryption of smc packet data.");
        }
    }

    private void manageNextSMCPacket(SMCProtocolPacket smcProtocolPacket) throws IOException, GeneralSecurityException {
        SMCProtocolPacket nextSMCProtocolPacket =
            mUserData.getDefaultUser().generateNextPacket(smcProtocolPacket);
        SerializableSMCProtocolPacket nextSerializableSMCProtocolPacket =
                new SerializableSMCProtocolPacket(nextSMCProtocolPacket);
        String stringSMCPacket = LoganSquare.serialize(nextSerializableSMCProtocolPacket);
        int thisindex = mTransportData.getDeviceList().indexOf(network.thisDevice);
        int nextDevice = (thisindex + 1) % mTransportData.getDeviceList().size();
        final SalutDevice receiver = mTransportData.getDeviceList().get(nextDevice);
        Log.d(TAG, "The receiver is " + receiver.deviceName);
        Log.d(TAG, "Building transport SMC protocol packet");
        TransportProtocolPacket transportProtocolPacket
                = buildTransportSMCProtocolPacket(stringSMCPacket, receiver);
        Log.d(TAG, "Building object packet");
        SalutObjectPacket salutObjectPacket =
                new SalutObjectPacket(transportProtocolPacket, network.thisDevice);
        salutObjectPacket.setReceiverDevice(receiver);
        logPacket("Sending SMC packet to receiver " + receiver.readableName + "[" + receiver.deviceName + "]");
        network.sendToHost(salutObjectPacket, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Failure when sending SMCPacket");
                notificateFailureDialog("Failure when sending SMCPacket to receiver "
                        + receiver.readableName);
            }
        });
    }

    private void sendSecurityInformation() {
        Log.i(TAG, "Sending security information to init device!");
        DeviceSecurityInfoPacket deviceSecurityInfoPacket = new DeviceSecurityInfoPacket();
        try {
            String pubKey = StringCypher.getPublicKeyString(mUserData.getKeyPair().getPublic());
            deviceSecurityInfoPacket.setPublicKey(pubKey);
            deviceSecurityInfoPacket.setDevice(network.thisDevice);
            try {
                SalutObjectPacket salutObjectPacket =
                        new SalutObjectPacket(deviceSecurityInfoPacket,network.thisDevice);
                logPacket("DeviceSecurityInfoPacket sent to init device ");
                network.sendToHost(salutObjectPacket, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(TAG, "Failure when sending deviceSecurityInfoPacket");
                        notificateFailureDialog("Failure when sending device security info");
                    }
                });
            } catch(IOException ioe){
                Log.e(TAG, "Failure when serializing deviceSecurityInfoPacket",ioe);
                notificateFailureDialog("Failure when serializing device security info");
            }
        }catch(GeneralSecurityException gse){
            Log.e(TAG, "Cannot serialize public key",gse);
            notificateFailureDialog("Cannot serialize public key, this is necessary to proceed!");
        }
    }

    private void initparams(){
        // fixme: must be done with isSecured..and so on
        if (Constants.PROTOCOL_NAME_MILLIONAIRE.equals(mUserData.getProtocolName())) {
            RSAPrivateKey rsaPriv =
                    (RSAPrivateKey) mUserData.getKeyPair().getPrivate();
            ((SecondMillionaire)mUserData.getDefaultUser())
                    .setB_privkey(rsaPriv.getPrivateExponent(), rsaPriv.getModulus());
        }
    }

    private void manageDevicesSecurityInfo(DevicesSecurityInfoPacket dsip) {
        logPacket("DevicesSecurityInfoPacket received");
        dsip.getDevicesSecurityPacket();
        mTransportData.setSecurityInfoPacketList(dsip.getDevicesSecurityPacket());
        logPacket("Sending confirmation packet to init device");
        ConfirmationPacket confirmationPacket = new ConfirmationPacket();
        confirmationPacket.setTypeOfPacketToConfirm(DevicesSecurityInfoPacket.class.getName());
        try {

            SalutObjectPacket salutObjectPacketWithConfirmation =
                    new SalutObjectPacket(confirmationPacket,network.thisDevice);
            network.sendToHost(salutObjectPacketWithConfirmation, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Failure when sending ConfirmationPacket");
                    notificateFailureDialog("Failure when sending start confirmation");
                }
            });
        } catch(IOException ioe){
            Log.e(TAG, "Failure when serializing ConfirmationPacket",ioe);
            notificateFailureDialog("Failure when serializing start confirmation");
        }
        initparams();
        mProtocolStatus = Constants.STATUS_RUNNING;
    }

    @NonNull
    private TransportProtocolPacket buildTransportSMCProtocolPacket(String stringSMCPacket, SalutDevice receiver) throws GeneralSecurityException {
        TransportProtocolPacket transportProtocolPacket =
                new TransportProtocolPacket();
        if(!(mUserData.getDefaultUser()).isSecured()){
            Log.d(TAG, "Retrieving the public key of receiver " + receiver.deviceName);
            String pk = mTransportData.getDevicePublicKey(receiver);
            Log.d(TAG, "Encrypting content for the receiver");
            String stringSMCPacketEncrypted =
                    StringCypher.encrypt(stringSMCPacket, pk);
            transportProtocolPacket.setIsEncrypted(true);
            transportProtocolPacket.setSerializedSMCProtocolPacket(stringSMCPacketEncrypted);
        } else {
            transportProtocolPacket.setIsEncrypted(false);
            transportProtocolPacket.setSerializedSMCProtocolPacket(stringSMCPacket);
        }
        return transportProtocolPacket;
    }

    private void logPacket(String s) { //xxx duplicate from initdeviceactivity
        mProtocolFragment.addLogItem(s);
        Log.d(TAG, "Protocol info: " + s);
    }

    @UiThread
    protected void notificateFailureDialog(String failureMessage){  //xxx duplicate from initdeviceactivity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Failure");
        builder.setMessage(failureMessage);
        builder.setCancelable(false);
        builder.setPositiveButton("Terminate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                return;
            }
        });
        builder.create().show();
    }

    private void sendFailure(String reason){
        FailurePacket failurePacket = new FailurePacket();
        failurePacket.setFailureReason(reason);
        try {

            SalutObjectPacket salutObjectPacketWithFailure =
                    new SalutObjectPacket(failurePacket,network.thisDevice);
            network.sendToHost(salutObjectPacketWithFailure, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Failure when sending failure packet");
                    notificateFailureDialog("Failure when sending failure packet");
                }
            });
        } catch(IOException ioe){
            Log.e(TAG, "Failure when serializing ConfirmationPacket",ioe);
            notificateFailureDialog("Failure when serializing start confirmation");
        }

    }
    @Override
    public void onBackPressed() { //xxx duplicate from initdeviceactivity
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }
}
