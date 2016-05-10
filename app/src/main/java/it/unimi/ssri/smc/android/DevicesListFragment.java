package it.unimi.ssri.smc.android;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.peak.salut.SalutDevice;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

@EFragment
@OptionsMenu(R.menu.menu_with_next)
public class DevicesListFragment extends Fragment {

    private static final String TAG = "DEVICES_LST_FRGMNT";

    @FragmentArg
    protected String mDeviceListTitle;

    @FragmentArg
    protected Integer mNOfDesiredDevices;

    // waiting
    protected ProgressBar mWait;

    private RecyclerView mSalutDeviceRecyclerView;

    private TextView mTextTitle;

    // an array of mSelected items
    private final ArrayList<SalutDevice> mSelected = new ArrayList<>();

    private List<SalutDevice> mSalutDevices = new ArrayList<>();

    private SalutDeviceAdapter mAdapter;

    private OnClickDeviceListener mOnClickDeviceListener;

    private NavigationListener mNavigationListener;

    private Boolean mSingleSelectionMode;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu occured");
        MenuItem continueMenuItem = menu.findItem(R.id.action_continue);
        if (continueMenuItem != null) {
            continueMenuItem.setVisible(mNOfDesiredDevices == mSelected.size());
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_continue) {
            mNavigationListener.onNextClicked();
            return true;
        }

        return false;
    }

    @Override  // for now android annotations cannot manage RecyclerView
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setSelectionMode();
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        mSalutDeviceRecyclerView = (RecyclerView) view
                .findViewById(R.id.devices_recycler_view);
        mSalutDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SalutDeviceAdapter(mSalutDevices);
        mSalutDeviceRecyclerView.setAdapter(mAdapter);
        mTextTitle = (TextView) view.findViewById(R.id.devices_list_title);
        mTextTitle.setText(mDeviceListTitle);
        mWait = (ProgressBar) view.findViewById(R.id.discovery_progress_bar);
        showWaitDialog();
        return view;
    }

    private void setSelectionMode() {
        if(mNOfDesiredDevices == null){
            mNOfDesiredDevices = -1;
            mSingleSelectionMode = null;
        } else if(mNOfDesiredDevices == 1){
            mSingleSelectionMode = true;
        } else if(mNOfDesiredDevices > 1){
            mSingleSelectionMode = false;
        }
    }

    @UiThread
    public void addDeviceToList(SalutDevice srcDevice) {
        if(View.VISIBLE == mWait.getVisibility()){
            hideWaitDialog();
        }
        if(mSalutDevices.contains(srcDevice)) {
            mSalutDevices.remove(srcDevice);
            mAdapter.notifyDataSetChanged();
        }
        mSalutDevices.add(srcDevice);
        mAdapter.notifyDataSetChanged();
        Log.i(TAG, "Device " + srcDevice.deviceName + " added to list");
    }

    @UiThread
    protected void showWaitDialog() {
        if (isVisible()) {
            mWait.setVisibility(View.VISIBLE);
            mSalutDeviceRecyclerView.setVisibility(View.GONE);
        }
    }

    @UiThread
    protected void hideWaitDialog() {
        if (isVisible()){
            mWait.setVisibility(View.GONE);
            mSalutDeviceRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class SalutDeviceHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView mSalutDeviceIcon;
        public TextView mSalutDeviceName;
        public TextView mSalutDeviceAddress;
        public TextView mSalutDeviceStatus;
        public ImageView mSalutDeviceChecked;

        private SalutDevice mSalutDevice;
        public SalutDeviceHolder(View itemView) {
            super(itemView);
            mSalutDeviceIcon = (ImageView)
                    itemView.findViewById(R.id.list_item_deviceicon);
            mSalutDeviceName =(TextView)
                    itemView.findViewById(R.id.list_item_devicename);
            mSalutDeviceAddress = (TextView)
                    itemView.findViewById(R.id.list_item_deviceaddress);
            mSalutDeviceStatus = (TextView)
                    itemView.findViewById(R.id.list_item_devicestatus);
            mSalutDeviceChecked = (ImageView)
                    itemView.findViewById(R.id.list_item_devicechecked);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mOnClickDeviceListener != null)
                mOnClickDeviceListener.onClick(mSalutDevice);
            if(mSingleSelectionMode == null){
                return;
            }
            if(mSingleSelectionMode){
                if(mSelected.contains(mSalutDevice)){
                    mSelected.remove(mSalutDevice);
                } else {
                    mSelected.clear();
                    mSelected.add(mSalutDevice);
                }
            } else {
                if(mSelected.contains(mSalutDevice)){
                    mSelected.remove(mSalutDevice);
                } else {
                    mSelected.add(mSalutDevice);
                }
            }
            mAdapter.notifyDataSetChanged();
            getActivity().invalidateOptionsMenu(); //todo move me
        }

        public void bindSalutDevice(SalutDevice device) {
            mSalutDevice = device;
            mSalutDeviceName.setText(device.deviceName + " [" + device.readableName + "]");
            mSalutDeviceAddress.setText(device.serviceName);
            if(mSalutDevice.isRegistered){
                mSalutDeviceStatus.setText("device registered");
                mSalutDeviceStatus.setVisibility(View.VISIBLE);
            } else {
                mSalutDeviceStatus.setVisibility(View.GONE);
            }
            if(mSelected.contains(device)){
                mSalutDeviceChecked.setVisibility(View.VISIBLE);
            } else {
                mSalutDeviceChecked.setVisibility(View.GONE);
            }

        }

    }

    private class SalutDeviceAdapter extends RecyclerView.Adapter<SalutDeviceHolder> {
        private List<SalutDevice> mSalutDevices;

        public SalutDeviceAdapter(List<SalutDevice> devices) {
            mSalutDevices = devices;
        }

        @Override
        public SalutDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.service_row, parent, false);
            return new SalutDeviceHolder(view);
        }

        @Override
        public void onBindViewHolder(SalutDeviceHolder holder, int position) {
            SalutDevice device = mSalutDevices.get(position);
            holder.bindSalutDevice(device);
        }

        @Override
        public int getItemCount() {
            return mSalutDevices.size();
        }

        public void setOnClickDeviceListener(OnClickDeviceListener onClickDeviceListener) {
            mOnClickDeviceListener = onClickDeviceListener;
        }
    }



    public List<SalutDevice> getSelected(){
        return mSelected;
    }

    public void setOnNextListener(NavigationListener onNextClicked) {
        mNavigationListener = onNextClicked;
    }

    public interface OnClickDeviceListener {
        void onClick(SalutDevice salutDevice);
    }

    public interface NavigationListener {
        void onNextClicked();
    }
}
