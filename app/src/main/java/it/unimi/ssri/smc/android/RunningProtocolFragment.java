package it.unimi.ssri.smc.android;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;

@EFragment
public class RunningProtocolFragment extends Fragment {

    private ArrayAdapter<String> mLogsAdapter;

    @InstanceState
    protected ArrayList<String> mLogArray = new ArrayList<>();

    private ListView mPackageExchangeList;
    private ProgressBar mProgressBar;

    @Override  // for now android annotations cannot manage Listview so good
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        View view = inflater.inflate(R.layout.fragment_running_protocol, container, false);
        mPackageExchangeList = (ListView) view.findViewById(R.id.running_logs);
        mLogsAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mLogArray);
        mPackageExchangeList.setAdapter(mLogsAdapter);
        mProgressBar = (ProgressBar) view.findViewById(R.id.running_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        return view;
    }

    public void addLogItem(String logItem){
        mLogArray.add(logItem);
        if(isVisible()) {
            mLogsAdapter.notifyDataSetChanged();
        }
    }

    @UiThread
    public void showLastStatus() {
        mProgressBar.setVisibility(View.GONE);
    }

}
