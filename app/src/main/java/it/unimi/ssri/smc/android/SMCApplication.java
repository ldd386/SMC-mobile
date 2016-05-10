package it.unimi.ssri.smc.android;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

/**
 * Created by K.M on 20/10/15.
 */
@EApplication
public class SMCApplication extends Application implements Foreground.Listener {

    @Override
    public void onBecameForeground() {

    }

    @Override
    public void onBecameBackground() {

    }
}
