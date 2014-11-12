package org.madn3s.controller;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController.Mode;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.components.CameraSelectionDialogFragment;
import org.madn3s.controller.fragments.BaseFragment;
import org.madn3s.controller.fragments.ConnectionFragment;
import org.madn3s.controller.fragments.RemoteControlFragment;
import org.madn3s.controller.fragments.DiscoveryFragment;
import org.madn3s.controller.fragments.ScannerFragment;
import org.madn3s.controller.fragments.SettingsFragment;
import org.madn3s.controller.io.BraveHeartMidgetService;
import org.madn3s.controller.io.HiddenMidgetReader;
import org.madn3s.controller.io.HiddenMidgetWriter;
import org.madn3s.controller.io.UniversalComms;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends Activity implements 
		BaseFragment.OnItemSelectedListener
		, CameraSelectionDialogFragment.DialogListener {

	private static final String tag = MainActivity.class.getSimpleName();
    private CharSequence mTitle;
	private FragmentManager mFragmentManager;
	private DiscoveryFragment mDiscoveryFragment;
	
	private BaseLoaderCallback mLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
        	mDiscoveryFragment = new DiscoveryFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mDiscoveryFragment)
                    .commit();
        }

        initializeSharedPrefs();
        setUpBridges();
		
		Intent williamWallaceIntent = new Intent(this, BraveHeartMidgetService.class);
		startService(williamWallaceIntent);
        
        MADN3SController.pointsTest();
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(!MADN3SController.isOpenCvLoaded) {
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
		}
	}

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
		case R.id.action_settings:
			launchSettingsFragment();
            return true;

		default:
			break;
		}
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onObjectSelected(Object selected, BaseFragment fragment) {
    	Mode mode = (Mode) selected;
    	
    	mFragmentManager.beginTransaction()
    		.remove(fragment)
    		.commit();
    	//TODO revisar casos de switch
    	/*TODO esto se está llamando desde DiscoveryFragment cableado 
    	 * como SCANNER y este llama es a ConnectionFragment, 
    	 * este switch debería llamarse desde ConnectionFragment*/
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
		finishCommunications();
		super.onDestroy();
	}
    
    /**
	 * Sets up OpenCV Init Callback and all <code>UniversalComms</code> Bridges and Callbacks
	 */
	private void setUpBridges() {
		
		mLoaderCallback = new BaseLoaderCallback(this) {
		       @Override
		       public void onManagerConnected(int status) {
		           switch (status) {
		               case LoaderCallbackInterface.SUCCESS:
		                   Log.i(tag, "OpenCV loaded successfully");
		                   MADN3SController.isOpenCvLoaded = true;
		                   break;
		               default:
		                   super.onManagerConnected(status);
		                   break;
		           }
		       }
		   };
		
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
	}

	/**
	 * Sends <code>abort</code> signal to cameras and robot and 
	 * closes communication channels with them
	 */
	private void finishCommunications() {
		try {
			JSONObject nxtJson = new JSONObject();
	        nxtJson.put("command", "abort");
	        nxtJson.put("action", "abort");
			
	        MADN3SController.talker.write(nxtJson.toString().getBytes());
			
			JSONObject json = new JSONObject();
	        json.put("action", "exit_app");
	        json.put("side", "left");
	        
	        HiddenMidgetWriter sendRightCamera = new HiddenMidgetWriter(
	        		MADN3SController.rightCameraWeakReference, json.toString());
	        sendRightCamera.execute();
	        
	        json.put("side", "right");
	        
	        HiddenMidgetWriter sendLeftCamera = new HiddenMidgetWriter(
	        		MADN3SController.leftCameraWeakReference, json.toString());
	        sendLeftCamera.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MADN3SController.isRunning.set(false);
		stopService(new Intent(this, BraveHeartMidgetService.class));
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
			if(!MADN3SController.sharedPrefsGetBoolean("loaded")){
				MADN3SController.sharedPrefsPutInt("speed", 15);
				MADN3SController.sharedPrefsPutFloat("radius", 45.0f);
				MADN3SController.sharedPrefsPutInt("points", 6);
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
			} 
		} catch (Exception e) {
			Log.d(tag, "Exception. Could not initialize SharedPrefs");
			e.printStackTrace();
		}
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		if(mDiscoveryFragment != null){
			mDiscoveryFragment.onDevicesSelectionCompleted();
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		if(mDiscoveryFragment != null){
			mDiscoveryFragment.onDevicesSelectionCancelled();
		}
	}
}
