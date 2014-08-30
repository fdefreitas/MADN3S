package org.madn3s.controller.components;

import java.io.IOException;
import java.lang.ref.WeakReference;




import org.madn3s.controller.io.HiddenMidgetReader;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.util.Log;

public class BraveHeartMidgetService extends IntentService {
	
	private final String tag = "BraveHeartMidgetService";

	public BraveHeartMidgetService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG) || intent.hasExtra("result")){
    		Log.d(tag, "Onstart Command. Llamando a onHandleIntent.");
    		return super.onStartCommand(intent,flags,startId);
    	} else {
	        Log.d(tag, "Onstart Command");
	        String stopservice = intent.getStringExtra("stopservice");
	        if (stopservice != null && stopservice.length() > 0) {
	            stopSelf();
	        }
	        return START_NOT_STICKY;
    	}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}

}
