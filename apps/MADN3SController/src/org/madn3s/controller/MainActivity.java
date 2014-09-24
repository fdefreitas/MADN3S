package org.madn3s.controller;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.fragments.BaseFragment;
import org.madn3s.controller.fragments.ConnectionFragment;
import org.madn3s.controller.fragments.RemoteControlFragment;
import org.madn3s.controller.fragments.DiscoveryFragment;
import org.madn3s.controller.fragments.NavigationDrawerFragment;
import org.madn3s.controller.fragments.ScannerFragment;
import org.madn3s.controller.fragments.SettingsFragment;
import org.madn3s.controller.io.BraveHeartMidgetService;
import org.madn3s.controller.io.HiddenMidgetReader;
import org.madn3s.controller.io.HiddenMidgetWriter;
import org.madn3s.controller.io.UniversalComms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks
        , BaseFragment.OnItemSelectedListener {

	private static final String tag = "MainActivity";
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
	private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        mFragmentManager = getFragmentManager();
        
        //TODO mover de aqui
        MADN3SController.isPictureTaken = new AtomicBoolean(true);
        MADN3SController.isRunning = new AtomicBoolean(true);
        MADN3SController.readCamera1 = new AtomicBoolean(false);
        MADN3SController.readCamera2 = new AtomicBoolean(false);
        
        //TODO validar si se debe llamar sin verificar ningún tipo de condición primero
        initializeSharedPrefs();
        
        HiddenMidgetReader.bridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveHeartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		ScannerFragment.bridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveHeartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_SEND, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		NXTTalker.bridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveHeartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_NXT_MESSAGE, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		SettingsFragment.bridge = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveHeartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_SEND, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		Intent williamWallaceIntent = new Intent(this, BraveHeartMidgetService.class);
		startService(williamWallaceIntent);
        
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
        	launchSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onObjectSelected(Object selected, BaseFragment fragment) {
    	Mode mode = (Mode) selected;
    	mFragmentManager.beginTransaction()
    		.remove(fragment)
    		.commit();
        switch (mode){
        	case CONTROLLER:
        		launchRemoteControlFragment();
        		break;
        	case SCANNER:
        		launchConnectionFragment();
        		break;
        	case SCAN:
        		launchScannerFragment();
        		break;
    		default:
    			
        }
    }
    
    @Override
	protected void onDestroy() {
		try {
			JSONObject nxtJson = new JSONObject();
	        nxtJson.put("command", "abort");
	        nxtJson.put("action", "abort");
			
	        MADN3SController.talker.write(nxtJson.toString().getBytes());
			
			JSONObject json = new JSONObject();
	        json.put("action", "exit_app");
	        json.put("side", "left");
	        
	        HiddenMidgetWriter sendCamera1 = new HiddenMidgetWriter(MADN3SController.camera1WeakReference, json.toString());
	        sendCamera1.execute();
	        
	        json.put("side", "right");
	        
	        HiddenMidgetWriter sendCamera2 = new HiddenMidgetWriter(MADN3SController.camera2WeakReference, json.toString());
	        sendCamera2.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MADN3SController.isRunning.set(false);
		stopService(new Intent(this, BraveHeartMidgetService.class));
		super.onDestroy();
	}

    
    /**
     * Method to replace current Fragment by {@link RemoteControlFragment}
     */
    public void launchRemoteControlFragment(){
        Log.d(tag, "launchRemoteControlFragment");
        RemoteControlFragment remoteControlFragment = new RemoteControlFragment();
        mFragmentManager.beginTransaction().replace(R.id.container, remoteControlFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(String.valueOf(remoteControlFragment.getClass()))
                .commit();
    }

    /**
     * Method to replace current Fragment by {@link ConnectionFragment}
     */
    public void launchConnectionFragment(){
        Log.d(tag, "launchConectionFragment");
        ConnectionFragment connectionFragment = new ConnectionFragment();
        mFragmentManager.beginTransaction()
        		.replace(R.id.container, connectionFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }
    
    /**
     * Method to replace current Fragment by {@link ScannerFragment}
     */
    public void launchScannerFragment(){
        Log.d(tag, "launchScannerFragment");
        ScannerFragment scannerFragment = new ScannerFragment();
        mFragmentManager.beginTransaction()
        		.replace(R.id.container, scannerFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }
    
    /**
     * Method to replace current Fragment by {@link SettingsFragment}
     */
    public void launchSettingsFragment(){
        SettingsFragment settingsFragment = new SettingsFragment();
        mFragmentManager.beginTransaction()
        		.replace(R.id.container, settingsFragment)
        		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }
	
    /**
     * Method to initialize SharedPreferences with default values
     */
	private void initializeSharedPrefs() {
		try {
			MADN3SController.sharedPrefsPutInt("speed", 15);
			MADN3SController.sharedPrefsPutFloat("radius", 99.0f);
			MADN3SController.sharedPrefsPutInt("points", 7);
			MADN3SController.sharedPrefsPutInt("p1x", 0);
			MADN3SController.sharedPrefsPutInt("p1y", 0);
			MADN3SController.sharedPrefsPutInt("p2x", 1);
			MADN3SController.sharedPrefsPutInt("p2y", 1);
			MADN3SController.sharedPrefsPutInt("iterations", 1);
			MADN3SController.sharedPrefsPutInt("maxCorners", 50);
			MADN3SController.sharedPrefsPutFloat("qualityLevel", (float) 0.01);
			MADN3SController.sharedPrefsPutInt("minDistance", 30);
			MADN3SController.sharedPrefsPutFloat("upperThreshold", (float) 75);
			MADN3SController.sharedPrefsPutFloat("lowerThreshold", (float) 35);
			MADN3SController.sharedPrefsPutInt("dDepth", 0);
			MADN3SController.sharedPrefsPutInt("dX", 0);
			MADN3SController.sharedPrefsPutInt("dY", 0);
			MADN3SController.sharedPrefsPutString("algorithm", "Canny");
			MADN3SController.sharedPrefsPutInt("algorithmIndex", R.id.canny_radio);
			MADN3SController.sharedPrefsPutBoolean("clean", false);
		} catch (Exception e) {
			Log.d(tag, "Exception. Could not initialize SharedPrefs");
			e.printStackTrace();
		}
	}
}
