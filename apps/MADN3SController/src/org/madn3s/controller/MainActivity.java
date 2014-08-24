package org.madn3s.controller;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.fragments.BaseFragment;
import org.madn3s.controller.fragments.ConnectionFragment;
import org.madn3s.controller.fragments.ControlsFragment;
import org.madn3s.controller.fragments.DiscoveryFragment;
import org.madn3s.controller.fragments.NavigationDrawerFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks
        , BaseFragment.OnItemSelectedListener {

	private static final String tag = "MainActivity";
	
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        MADN3SController.isPictureTaken = new AtomicBoolean(true);
        MADN3SController.isRunning = new AtomicBoolean(true);
        
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DiscoveryFragment())
                    .commit();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new DiscoveryFragment())
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onObjectSelected(Object selected) {
    	Mode mode = (Mode) selected;
        switch (mode){
        	case CONTROLLER:
        		launchControlsFragment();
        		break;
        	case SCANNER:
        		launchConnectionFragment();
        		break;
    		default:
        }
    }

    public void launchControlsFragment(){
        Log.d(tag, "launchControlsFragment");
        ControlsFragment controls = new ControlsFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, controls)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(String.valueOf(controls.getClass()))
                .commit();
        fm.executePendingTransactions();
    }

    public void launchConnectionFragment(){
        Log.d(tag, "launchConectionFragment");
        ConnectionFragment conections = new ConnectionFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, conections)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(String.valueOf(conections.getClass()))
                .commit();
        fm.executePendingTransactions();
    }

}
