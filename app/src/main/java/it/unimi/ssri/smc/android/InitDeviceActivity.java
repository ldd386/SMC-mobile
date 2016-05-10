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
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import it.unimi.ssri.smc.protocols.problem.InitUser;
import it.unimi.ssri.smc.protocols.problem.millionaire.InitMillionaire;
import it.unimi.ssri.smc.protocols.problem.voting.InitVoter;

@EActivity(R.layout.activity_single_fragment)
public class InitDeviceActivity extends AppCompatActivity implements SalutDataCallback {
    private static final String TAG = "INIT_DEVICE_ACTIVITY";

    @Bean
    protected UserData mUserData;

    @Bean
    protected TransportData mTransportData;

    private Salut network;

    private DevicesListFragment mDevicesListFragment;
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
        int nOfDesiredDevices = ((InitUser)mUserData.getDefaultUser()).getGroupSize()-1;
        mDevicesListFragment = DevicesListFragment_.builder()
                .mDeviceListTitle("Waiting for device connection..")
                .mNOfDesiredDevices(nOfDesiredDevices).build();
        mDevicesListFragment.setOnNextListener(new DevicesListFragment.NavigationListener() {

            @Override
            public void onNextClicked() {
                List<SalutDevice> selectedDevices = mDevicesListFragment.getSelected();
                getFragmentManager().beginTransaction().remove(mDevicesListFragment).commit();
                mTransportData.setDeviceList(selectedDevices);
                initParamsPhase();
            }
        });
        mProtocolFragment = RunningProtocolFragment_.builder().build();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mProtocolFragment).commit();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mDevicesListFragment).commit();
    }

    @Background(delay = 3000)
    protected void asyncServiceRegistration() {
        if(isFinishing()) return;
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, this);
        SalutServiceData serviceData = new SalutServiceData(Constants.SERVICE_PREFIX + mUserData.getProtocolName(),
                Constants.SERVICE_PORT, mUserData.getDeviceName());
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
                notificateFailureDialog("Sorry, but this device does not support WiFi Direct.");
                mProtocolStatus = Constants.STATUS_FINISHED;
            }
        });

        network.startNetworkService(new SalutDeviceCallback() {
            @Override //onDeviceRegistered =Callback
            public void call(SalutDevice defaultDevice) {
                if (mProtocolStatus == Constants.STATUS_SYNCING) {
                    Log.i(TAG, defaultDevice.readableName + " has connected!");
                    mDevicesListFragment.addDeviceToList(defaultDevice);
                } else {
                    Log.i(TAG, defaultDevice.readableName + " tried to connect! Not possible in this phase");
                }
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                // on success
                Log.i(TAG, "Success when starting network service");
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                // on failure
                Log.e(TAG, "Failure when starting network service");
                network.stopNetworkService(false);
                notificateFailureDialog("Failure when starting network service, please re-run app");
                mProtocolStatus = Constants.STATUS_FINISHED;
            }
        });

    }

    @Override
    public void onDestroy() {
        network.stopNetworkService(false);
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
            SalutObjectPacket salutObjectPacket = LoganSquare.parse((String)o, SalutObjectPacket.class);
            Log.i(TAG, "Type of packet received is: " + salutObjectPacket.getTypeOfPacket());
            if (salutObjectPacket.isPacketOfType(DeviceSecurityInfoPacket.class)){
                DeviceSecurityInfoPacket dsip = (DeviceSecurityInfoPacket) salutObjectPacket.getPacket();
                manageDeviceSecurityPacket(dsip);
            } else if (salutObjectPacket.isPacketOfType(ConfirmationPacket.class)){
                ConfirmationPacket cp = (ConfirmationPacket) salutObjectPacket.getPacket();
                SalutDevice senderDevice = salutObjectPacket.getSenderDevice();
                manageConfirmationPacket(cp, senderDevice);
            } else if(salutObjectPacket.isPacketOfType(TransportProtocolPacket.class)) {
                TransportProtocolPacket tpp = (TransportProtocolPacket) salutObjectPacket.getPacket();
                logPacket("Transport Protocol packet received!");
                if(network.thisDevice.equals(salutObjectPacket.getReceiverDevice())) {
                    logPacket("Transport Protocol packet is for this device.");
                    manageSMCTransport(tpp);
                } else {
                    network.sendToDevice(salutObjectPacket.getReceiverDevice(), salutObjectPacket, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.e(TAG, "Failed to forward transport packet.");
                            notificateFailureDialog("Failed to forward transport packet.");
                            mProtocolStatus = Constants.STATUS_FINISHED;
                        }
                    });
                }
            } else if (salutObjectPacket.isPacketOfType(FailurePacket.class)) {
                logPacket("Failure packet received");
                network.sendToAllDevices(salutObjectPacket, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(TAG, "Failed to forward failure packet.");
                        notificateFailureDialog("Failed to forward failure packet.");
                        mProtocolStatus = Constants.STATUS_FINISHED;
                    }
                });
                notificateFailureDialog(((FailurePacket) salutObjectPacket.getPacket()).getFailureReason());
                mProtocolStatus = Constants.STATUS_FINISHED;

            }
        } catch (IOException|ClassNotFoundException ex){
            Log.e(TAG, "Failed to parse network data.",ex);
            notificateFailureDialog("Failed to parse network data.");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
    }

    private void initThisDeviceParams() throws GeneralSecurityException {
        mTransportData.getDeviceList().add(network.thisDevice);
        DeviceSecurityInfoPacket dsip =
                new DeviceSecurityInfoPacket();
        dsip.setDevice(network.thisDevice);
        String thisPublicKey =
                StringCypher.getPublicKeyString(mUserData.getKeyPair().getPublic());
        dsip.setPublicKey(thisPublicKey);
        mTransportData.setSecurityInfoPacketList(new ArrayList<DeviceSecurityInfoPacket>());
        mTransportData.getSecurityInfoPacketList().add(dsip);
    }

    private void initParamsPhase() {
        try{
            initThisDeviceParams();
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failure when adding this device to devices list", e);
            notificateFailureDialog("Failure when adding this device to devices list");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
        mProtocolStatus = Constants.STATUS_PREPARING;
        ProtocolParamsPacket protocolParamsPacket = new ProtocolParamsPacket();
        protocolParamsPacket.setIsEncryptionOn(true);
        protocolParamsPacket.setDevicesInComputation(mTransportData.getDeviceList());
        if(Constants.PROTOCOL_NAME_VOTING.equals(mUserData.getProtocolName())) {
            VotingExtraDataPacket votingExtraDataPacket = new VotingExtraDataPacket();
            votingExtraDataPacket.setNumberOfCandidates(
                    ((InitVoter) mUserData.getDefaultUser()).getNVoters());
            try {
                String votingExtraDataString = LoganSquare.serialize(votingExtraDataPacket);
                protocolParamsPacket.setOtherSerializedData(votingExtraDataString);
            } catch (IOException ioe){
                Log.e(TAG, "Failure when adding extra data for voting protocol in protocolParamsPacket",ioe);
                notificateFailureDialog("Failure when adding extra data in protocol parameters to devices");
                mProtocolStatus = Constants.STATUS_FINISHED;
            }
        }
        try {
            SalutObjectPacket salutObjectPacket = new SalutObjectPacket(protocolParamsPacket);
            logPacket("Sending ProtocolParamsPacket to all.");
            network.sendToAllDevices(salutObjectPacket, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Failure when sending protocolParamsPacket");
                    notificateFailureDialog("Failure when sending protocol parameters to devices");
                    mProtocolStatus = Constants.STATUS_FINISHED;
                }
            });
        } catch(IOException ioe){
            Log.e(TAG, "Failure when serializing protocolParamsPacket",ioe);
            notificateFailureDialog("Failure when serializing protocol parameters to devices");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
    }

    private void manageDeviceSecurityPacket(DeviceSecurityInfoPacket dsip) {
        String from = dsip.getDevice() != null?   dsip.getDevice().readableName + "[" + dsip.getDevice().deviceName + "]" : null;
        logPacket("Received DeviceSecurityInfoPacket from " + from);
        mTransportData.getSecurityInfoPacketList().add(dsip);
        boolean isSecurityDataCollectionFinished = mTransportData.allDeviceHasSecurityInfo();
        if(isSecurityDataCollectionFinished){
            sendSecurityInfoToAll();
        }
    }

    private void sendSecurityInfoToAll() {
        initGeneralParams();
        DevicesSecurityInfoPacket devicesSecurityInfoPacket = new DevicesSecurityInfoPacket();
        devicesSecurityInfoPacket.setDevicesSecurityPacket(
                new ArrayList<>(mTransportData.getSecurityInfoPacketList()));
        try {
            SalutObjectPacket salutObjectPacket =
                    new SalutObjectPacket(devicesSecurityInfoPacket,network.thisDevice);
            logPacket("Sending DevicesSecurityInfoPacket to all.");
            network.sendToAllDevices(salutObjectPacket, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Failure when sending devicesSecurityInfoPacket");
                    notificateFailureDialog("Failure when sending device security info");
                    mProtocolStatus = Constants.STATUS_FINISHED;
                }
            });
        } catch(IOException ioe){
            Log.e(TAG, "Failure when serializing devicesSecurityInfoPacket",ioe);
            notificateFailureDialog("Failure when serializing device security info");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
    }

    private void initGeneralParams() {
        try {
            if (Constants.PROTOCOL_NAME_MILLIONAIRE.equals(mUserData.getProtocolName())) {
                if (mUserData.isInitUser()) {
                    for (SalutDevice sd : mTransportData.getDeviceList()) {
                        if (!sd.equals(network.thisDevice)) {
                            String pubKeyDest = mTransportData.getDevicePublicKey(sd);
                            RSAPublicKey rsaPub =
                                    (RSAPublicKey) StringCypher.loadPublicKey(pubKeyDest);
                            ((InitMillionaire) mUserData.getDefaultUser())
                                    .setB_pubkey(rsaPub.getPublicExponent(), rsaPub.getModulus());
                        }
                    }
                }
            }
        }catch(GeneralSecurityException gse){
            notificateFailureDialog("Cannot retrieve the public exponent for the secondary user.");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
    }

    private HashMap<String,List<SalutDevice>> mSalutDeviceWithFeedback = new HashMap<>();

    private void manageConfirmationPacket(ConfirmationPacket cp, SalutDevice senderDevice) {
        logPacket("Confirmation packet received");
        List<SalutDevice> lsd = mSalutDeviceWithFeedback.get(cp.getTypeOfPacketToConfirm());
        if(lsd == null){
            lsd = new ArrayList<>();
            mSalutDeviceWithFeedback.put(cp.getTypeOfPacketToConfirm(),lsd);
        }
        lsd.add(senderDevice);
        if(DevicesSecurityInfoPacket.class.getName().equals(cp.getTypeOfPacketToConfirm())){
            List<SalutDevice> devicesReadyToStart =
                    mSalutDeviceWithFeedback.get(cp.getTypeOfPacketToConfirm());
            devicesReadyToStart.add(network.thisDevice);
            boolean canIStart = devicesReadyToStart != null
                    && devicesReadyToStart.containsAll(mTransportData.getDeviceList());
            if(canIStart){
                mProtocolStatus = Constants.STATUS_RUNNING;
                sendFirstSMCPacket();
            }
        }
    }

    private void sendFirstSMCPacket() {
        try {
            SMCProtocolPacket smcProtocolPacket =
                    ((InitUser) mUserData.getDefaultUser()).generateInitializationPacket();
            SerializableSMCProtocolPacket serializableSMCProtocolPacket =
                    new SerializableSMCProtocolPacket(smcProtocolPacket);
            String stringSMCPacket = LoganSquare.serialize(serializableSMCProtocolPacket);
            SalutDevice receiver = mTransportData.getDeviceList().get(0);
            Log.d(TAG, "Building FIRST transport SMC protocol packet");
            TransportProtocolPacket transportProtocolPacket
                    = buildTransportSMCProtocolPacket(stringSMCPacket, receiver);
            Log.d(TAG, "Building object packet");
            SalutObjectPacket salutObjectPacket =
                    new SalutObjectPacket(transportProtocolPacket, network.thisDevice);
            salutObjectPacket.setReceiverDevice(receiver);
            logPacket("Sending first SMC packet to receiver " + receiver.readableName);
            network.sendToDevice(receiver, salutObjectPacket, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Failure when sending first SMCPacket");
                    notificateFailureDialog("Failure when sending first SMCPacket");
                    mProtocolStatus = Constants.STATUS_FINISHED;
                }
            });
        }catch (IOException|GeneralSecurityException e){
            Log.e(TAG, "Serialization of first SMCPacket failed",e);
            notificateFailureDialog("Failure when serializing first SMCPacket");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }

    }

    private void manageSMCTransport(TransportProtocolPacket tsp) throws IOException {
        String maybeEncryptedPacket = tsp.getSerializedSMCProtocolPacket();
        String decryptedPacket = null;
        try {
             decryptedPacket = tsp.getIsEncrypted()?
                    StringCypher.decrypt(maybeEncryptedPacket,
                            mUserData.getKeyPair().getPrivate()):maybeEncryptedPacket;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed in decryption of smc packet data.");
            notificateFailureDialog("Failed in decryption of smc packet data.");
            mProtocolStatus = Constants.STATUS_FINISHED;
        }
        SerializableSMCProtocolPacket serializableSMCProtocolPacket =
                LoganSquare.parse(decryptedPacket, SerializableSMCProtocolPacket.class);
        SMCProtocolPacket smcProtocolPacket = serializableSMCProtocolPacket.getPacket();
        SMCProtocolPacket nextSMCProtocolPacket =
                mUserData.getDefaultUser().generateNextPacket(smcProtocolPacket);
        SerializableSMCProtocolPacket nextSerializableSMCProtocolPacket =
                new SerializableSMCProtocolPacket(nextSMCProtocolPacket);

        String stringOfNextSMCPacket = LoganSquare.serialize(nextSerializableSMCProtocolPacket);
        if (!nextSMCProtocolPacket.isLastPacket()) {
            //Log.d(TAG, "sending packet: " + stringSMCPacket);
            manageSMCTransportStandardPacket(stringOfNextSMCPacket);
        } else {
            Log.d(TAG, "this is the last packet: " + stringOfNextSMCPacket);
            manageSMCTransportLastPacket(stringOfNextSMCPacket);
            String verboseResult = mUserData.getDefaultUser().getVerboseResult(nextSMCProtocolPacket);
            logPacket(verboseResult);
            mProtocolFragment.showLastStatus();
        }

    }

    private void manageSMCTransportLastPacket(String stringSMCPacket) throws IOException {
        Log.d(TAG, "This is the last SMC packet");
        TransportProtocolPacket transportProtocolPacket =
                new TransportProtocolPacket();
        transportProtocolPacket.setIsEncrypted(false);
        transportProtocolPacket.setSerializedSMCProtocolPacket(stringSMCPacket);
        Log.d(TAG, "Building object packet");
        SalutObjectPacket salutObjectPacket =
                new SalutObjectPacket(transportProtocolPacket, network.thisDevice);
        logPacket("Sending last SMC packet with result to all");
        mProtocolStatus = Constants.STATUS_FINISHED;
        network.sendToAllDevices(salutObjectPacket, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Failure when sending last SMCPacket");
                notificateFailureDialog("Failure when sending last SMCPacket.");
            }
        });
    }

    private void manageSMCTransportStandardPacket(String stringSMCPacket) throws IOException {
        int thisindex = mTransportData.getDeviceList().indexOf(network.thisDevice);
        int nextDevice = (thisindex + 1) % mTransportData.getDeviceList().size();
        final SalutDevice receiver = mTransportData.getDeviceList().get(nextDevice);
        Log.d(TAG, "Building transport SMC protocol packet");
        TransportProtocolPacket transportProtocolPacket = null;
        try {
             transportProtocolPacket = buildTransportSMCProtocolPacket(stringSMCPacket, receiver);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed in encryption of smc packet data.",e);
            notificateFailureDialog("Failed in encryption of smc packet data.");
            mProtocolStatus = Constants.STATUS_FINISHED;
            return;
        }
        Log.d(TAG, "Building object packet");
        SalutObjectPacket salutObjectPacket =
                new SalutObjectPacket(transportProtocolPacket, network.thisDevice);
        salutObjectPacket.setReceiverDevice(receiver);
        logPacket("Sending SMC packet to receiver " + receiver.readableName);
        network.sendToDevice(receiver, salutObjectPacket, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Failure when sending SMCPacket");
                notificateFailureDialog("Failure when sending SMCPacket to " + receiver.readableName);
                mProtocolStatus = Constants.STATUS_FINISHED;
            }
        });
    }

    @NonNull
    private TransportProtocolPacket buildTransportSMCProtocolPacket(String stringSMCPacket, SalutDevice receiver)
            throws GeneralSecurityException {

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

    private void logPacket(String s) {  //xxx duplicate
        mProtocolFragment.addLogItem(s);
        Log.d(TAG, "Protocol info: " + s);
    }

    @UiThread
    protected void notificateFailureDialog(String failureMessage){  //xxx duplicate
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

    @Override
    public void onBackPressed() {  //xxx duplicate
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
