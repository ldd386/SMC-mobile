package it.unimi.ssri.smc.android;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.peak.salut.Salut;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import it.unimi.ssri.smc.network.StringCypher;
import it.unimi.ssri.smc.protocols.problem.millionaire.InitMillionaire;
import it.unimi.ssri.smc.protocols.problem.millionaire.SecondMillionaire;
import it.unimi.ssri.smc.protocols.problem.voting.CommonVoter;
import it.unimi.ssri.smc.protocols.problem.voting.InitVoter;


@EFragment(R.layout.fragment_init)
@OptionsMenu(R.menu.menu_with_next)
public class MainFragment extends Fragment {

    private static final String TAG = "MAIN_FRGMNT";

    @ViewById(R.id.protocol_role)
    Switch mProtocolRole;

    @ViewById(R.id.input_layout_protocol)
    TextInputLayout mProtocolContainer;

    @ViewById(R.id.protocols_spinner)
    Spinner mProtocolSpinner;

    @ViewById(R.id.input_layout_devicename)
    TextInputLayout mDevicenameContainer;

    @ViewById(R.id.devicename)
    EditText mDevicename;

    @ViewById(R.id.voting_layout)
    LinearLayout mVotingLayout;

    @ViewById(R.id.input_layout_ncandidates)
    TextInputLayout mNCandidatesContainer;

    @ViewById(R.id.ncandidates)
    EditText mNCandidates;

    @ViewById(R.id.input_layout_nvoters)
    TextInputLayout mNVotersContainer;

    @ViewById(R.id.nvoters)
    EditText mNVoters;

    @ViewById(R.id.input_layout_my_vote)
    TextInputLayout mMyVoteContainer;

    @ViewById(R.id.my_vote)
    EditText mMyVote;

    @ViewById(R.id.millionaire_layout)
    LinearLayout mMilionaireLayout;

    @ViewById(R.id.input_layout_my_millions)
    TextInputLayout mMyMilionsContainer;

    @ViewById(R.id.my_millions)
    EditText mMyMillions;

    @Bean
    UserData mUserData;

    private List<String> mProtocols;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu occured");
        MenuItem continueMenuItem = menu.findItem(R.id.action_continue);
        if (continueMenuItem != null) {
            continueMenuItem.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_continue) {
            validateAndContinue();
            return true;
        }

        return false;
    }

    @AfterViews
    protected void populateSpinner(){
        // Spinner Drop down elements
        mProtocols = new ArrayList<>();
        mProtocols.add("(select protocol)");
        mProtocols.add(Constants.PROTOCOL_NAME_VOTING);
        mProtocols.add(Constants.PROTOCOL_NAME_MILLIONAIRE);

        mProtocolRole.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserData.setIsInitUser(isChecked);
                hideShowMilionaireInitUserData(isChecked);
            }
        });

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mProtocols);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProtocolSpinner.setAdapter(dataAdapter);

        // Spinner click listener
        mProtocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Constants.PROTOCOL_NAME_VOTING.equals(mProtocols.get(position))) {
                    mUserData.setProtocolName(Constants.PROTOCOL_NAME_VOTING);
                    // voting protocol selected
                    // int n_candidates, int n_voters, int my_vote
                    mVotingLayout.setVisibility(View.VISIBLE);
                    mMilionaireLayout.setVisibility(View.GONE);
                    hideShowMilionaireInitUserData(mProtocolRole.isChecked());
                } else if (Constants.PROTOCOL_NAME_MILLIONAIRE.equals(mProtocols.get(position))) {
                    mUserData.setProtocolName(Constants.PROTOCOL_NAME_MILLIONAIRE);
                    // millionaire protocol selected
                    // my richness
                    mVotingLayout.setVisibility(View.GONE);
                    mMilionaireLayout.setVisibility(View.VISIBLE);
                } else {
                    mUserData.setProtocolName(null);
                    mVotingLayout.setVisibility(View.GONE);
                    mMilionaireLayout.setVisibility(View.GONE);
                    mProtocolContainer.setError(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @UiThread
    protected void hideShowMilionaireInitUserData(boolean isInitUser){
        if(isInitUser) {
            mNVoters.setVisibility(View.VISIBLE);
            mNCandidates.setVisibility(View.VISIBLE);
        } else {
            mNVoters.setVisibility(View.GONE);
            mNCandidates.setVisibility(View.GONE);
        }
    }

    private static final int DEFAULT_DIM_U = 100;

    @UiThread
    protected void validateAndContinue(){
        // move these strings
        String pleaseSelectProtocol = "Please select a protocol before start";
        String cannotBeEmpty = "Cannot be empty";
        String mustBeFilledNumber = "Must be filled with a number";
        String errorInVote = "Vote must be the index of candidate and must be inside the candidates range";
        String errorCandidates = "N of Candidates must be >2";
        String maxValuesForMillions = "Max value for millions is ";
        String deviceName = mDevicename.getText().toString();
        if(TextUtils.isEmpty(deviceName)){
            mDevicenameContainer.setError(cannotBeEmpty);
            return;
        } else {
            mUserData.setDeviceName(deviceName);
        }
        if(TextUtils.isEmpty(mUserData.getProtocolName())){
            mProtocolContainer.setError(pleaseSelectProtocol);
            return;
        }
        if(mUserData.getKeyPair() == null) {
            try {
                mUserData.setKeyPair(StringCypher.generateKeyPair());
            } catch (GeneralSecurityException gse){
                Log.e(TAG,"Cannot generate key pair",gse);
                //todo show a snackbar
                return;
            }
        }
        Intent intent;
        if(mUserData.isInitUser()){
            if (Constants.PROTOCOL_NAME_VOTING.equals(mUserData.getProtocolName())) {
                String nCandidates = mNCandidates.getText().toString();
                String nVoters = mNVoters.getText().toString();
                String myVote = mMyVote.getText().toString();
                boolean hasError = false;
                if(TextUtils.isEmpty(nCandidates) || !TextUtils.isDigitsOnly(nCandidates)){
                    mNCandidatesContainer.setError(mustBeFilledNumber);
                    hasError = true;
                }
                if(TextUtils.isEmpty(nVoters) || !TextUtils.isDigitsOnly(nVoters)) {
                    mNVotersContainer.setError(mustBeFilledNumber);
                    hasError = true;
                }
                if(TextUtils.isEmpty(myVote) || !TextUtils.isDigitsOnly(myVote)){
                    mMyVoteContainer.setError(mustBeFilledNumber);
                    hasError = true;
                }
                if(!hasError){
                    int nCandidatesValue = Integer.parseInt(nCandidates);
                    int nVotersValue = Integer.parseInt(nVoters);
                    int myVoteValue = Integer.parseInt(myVote);
                    if(myVoteValue > nCandidatesValue-1 || myVoteValue < 0){
                        mMyVoteContainer.setError(errorInVote);
                        return;
                    }
                    if(nVotersValue < 3){
                        mNVotersContainer.setError(errorCandidates);
                        return;
                    }
                    InitVoter initVoter = new InitVoter(nCandidatesValue, nVotersValue, myVoteValue);
                    mUserData.setDefaultUser(initVoter);
                } else return;

            } else if (Constants.PROTOCOL_NAME_MILLIONAIRE.equals(mUserData.getProtocolName())) {
                String myMillions = mMyMillions.getText().toString();
                if(TextUtils.isEmpty(myMillions) || !TextUtils.isDigitsOnly(myMillions)){
                    mMyMilionsContainer.setError(mustBeFilledNumber);
                    return;
                }
                if(Integer.parseInt(myMillions) > DEFAULT_DIM_U){
                    mMyMilionsContainer.setError(maxValuesForMillions + DEFAULT_DIM_U) ;
                    return;
                }
                InitMillionaire initMillionaire = new InitMillionaire(new BigInteger(myMillions), DEFAULT_DIM_U);
                mUserData.setDefaultUser(initMillionaire);
            }
            intent = new Intent(getActivity(), InitDeviceActivity_.class);
        } else {
            if (Constants.PROTOCOL_NAME_VOTING.equals(mUserData.getProtocolName())) {
                String myVote = mMyVote.getText().toString();
                if(TextUtils.isEmpty(myVote) || !TextUtils.isDigitsOnly(myVote)){
                    mMyVoteContainer.setError(mustBeFilledNumber);
                    return;
                }
                CommonVoter commonVoter = new CommonVoter(Integer.parseInt(myVote));
                mUserData.setDefaultUser(commonVoter);
            } else if (Constants.PROTOCOL_NAME_MILLIONAIRE.equals(mUserData.getProtocolName())) {
                String myMillions = mMyMillions.getText().toString();
                if(TextUtils.isEmpty(myMillions) || !TextUtils.isDigitsOnly(myMillions)){
                    mMyMilionsContainer.setError(mustBeFilledNumber);
                    return;
                }
                if(Integer.parseInt(myMillions) > DEFAULT_DIM_U){
                    mMyMilionsContainer.setError(maxValuesForMillions + DEFAULT_DIM_U) ;
                    return;
                }
                SecondMillionaire secondMillionaire =
                        new SecondMillionaire(new BigInteger(myMillions), DEFAULT_DIM_U);
                mUserData.setDefaultUser(secondMillionaire);
            }
            intent = new Intent(getActivity(), SecondaryDeviceActivity_.class);

        }
        if(!Salut.isWiFiEnabled(getActivity())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Wifi disabled");
            builder.setMessage("Wifi is disabled, please enable it and try again.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            builder.create().show();
        } else {
            startActivity(intent);
            getActivity().finish();
        }
    }
}
