package it.unimi.ssri.smc.android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_single_fragment)
public class MainActivity extends AppCompatActivity {

    private static final String MAIN_TAG = "MAIN_TAG";

    @AfterInject
    protected void addSelectorFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Log.i(MAIN_TAG, "fragmentManager.getBackStackEntryCount() is "
                + fragmentManager.getBackStackEntryCount());
        Fragment mainFragment = MainFragment_.builder().build();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mainFragment, MAIN_TAG);
        fragmentTransaction.commit();
    }

}
