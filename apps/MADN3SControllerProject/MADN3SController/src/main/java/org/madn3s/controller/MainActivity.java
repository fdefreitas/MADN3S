package org.madn3s.controller;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.madn3s.controller.fragments.BaseFragment;
import org.madn3s.controller.fragments.ControlsFragment;
import org.madn3s.controller.fragments.MainFragment;
import org.madn3s.controller.fragments.ConnectionFragment;

import java.util.ArrayList;

public class MainActivity extends Activity implements BaseFragment.OnItemSelectedListener {
    String TAG = "DEBUG MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onObjectSelected(Object selected) {
        if(selected instanceof BluetoothDevice){
            launchControlsFragment((BluetoothDevice) selected);
        } else if(selected instanceof ArrayList){
            launchConectionFragment((ArrayList<BluetoothDevice>) selected);
        }
    }

    public void launchControlsFragment(BluetoothDevice device){
        Log.d(TAG, "launchControlsFragment Device: "+device.getName());
        ControlsFragment controls = new ControlsFragment(device);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, controls)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(String.valueOf(controls.getClass()))
                .commit();
        Log.d(TAG, "Fin de Transaction de Fragments");
    }

    public void launchConectionFragment(ArrayList<BluetoothDevice> devices){
        Log.d(TAG, "launchConectionFragment Device: "+devices.size());
        ConnectionFragment conections = new ConnectionFragment(devices);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, conections)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(String.valueOf(conections.getClass()))
                .commit();
        Log.d(TAG, "Fin de Transaction de Fragments");
    }
}
