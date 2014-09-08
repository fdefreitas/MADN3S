package org.madn3s.controller.io;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BraveHeartMidgetService extends IntentService {
	
	private static final String tag = "BraveHeartMidgetService";
	private static Handler mHandler = null;
	private final IBinder mBinder = new LocalBinder();
	private static JSONObject rightJson, leftJson;
	private static ArrayList<JSONObject> frames;
	
	public class LocalBinder extends Binder {
		 BraveHeartMidgetService getService() {
            return BraveHeartMidgetService.this;
        }
    }

	public BraveHeartMidgetService(String name) {
		super(name);
		rightJson = leftJson = null;
		frames = new ArrayList<JSONObject>();
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        mHandler = ((MADN3SController) getApplication()).getBluetoothHandler();
        Log.d(tag, "mHandler "+ mHandler == null ? "NULL" : mHandler.toString());
        Log.d(tag, "mBinder "+ mBinder == null ? "NULL" : mBinder.toString());
        return mBinder;
    }
	
	public BraveHeartMidgetService() {
		super(tag);
		rightJson = leftJson = null;
		frames = new ArrayList<JSONObject>();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG) || intent.hasExtra("result")){
    		return super.onStartCommand(intent,flags,startId);
    	} else {
	        String stopservice = intent.getStringExtra("stopservice");
	        if (stopservice != null && stopservice.length() > 0) {
	            stopSelf();
	        }
	        return START_NOT_STICKY;
    	}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String jsonString;
		JSONObject msg;
		if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG)){
			jsonString = intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_MSG);
			try {
				msg = new JSONObject(jsonString);
				if(msg.has("error") && !msg.getBoolean("error")){
					if(msg.has("side")){
						String side = msg.getString("side");
						if(side.equalsIgnoreCase("right")){
							rightJson = msg;
							rightJson.remove("side");
							rightJson.remove("time");
							rightJson.remove("error");
							rightJson.remove("camera");
							Log.d(tag, "right " + rightJson.toString());
						} else if(side.equalsIgnoreCase("left")){
							leftJson = msg;
							leftJson.remove("side");
							leftJson.remove("time");
							leftJson.remove("error");
							leftJson.remove("camera");
							Log.d(tag, "left " + leftJson.toString());
						}
						if(leftJson != null && rightJson != null){
							JSONObject frame = new JSONObject();
							frame.put("right", rightJson);
							frame.put("left", leftJson);
							Log.d(tag, "tengo ambas " + frame.toString());
							frames.add(frame);
							rightJson = leftJson = null;
						}
						Log.d(tag, "Llego " + side + " right " + (rightJson!=null) + " left " + (leftJson!=null) + " nFrames = " + frames.size());
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
