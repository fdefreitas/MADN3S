package org.madn3s.controller.io;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera1WeakReference;
import static org.madn3s.controller.MADN3SController.camera2;
import static org.madn3s.controller.MADN3SController.camera2WeakReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Device;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BraveHeartMidgetService extends IntentService {
	
	private static final String tag = "BraveHeartMidgetService";
	private static Handler mHandler = null;
	private final IBinder mBinder = new LocalBinder();
	public static UniversalComms scannerBridge;
	
	public class LocalBinder extends Binder {
		 BraveHeartMidgetService getService() {
            return BraveHeartMidgetService.this;
        }
    }

	public BraveHeartMidgetService(String name) {
		super(name);
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
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG) || intent.hasExtra("result") || intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_SEND) || intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_NXT_MESSAGE)){
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
			processAnswer(jsonString);
		} else if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_SEND)){
			jsonString = intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_SEND);
			sendMessageToCameras(jsonString);
		} else if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_NXT_MESSAGE)){
			Bundle bundle = new Bundle();
			bundle.putInt("state", org.madn3s.controller.MADN3SController.State.CONNECTED.getState());
			bundle.putInt("device", Device.NXT.getValue());
			scannerBridge.callback(bundle);
			sendMessageToCameras();
		} 
	}
	
	public void sendMessageToCameras(){
		try{
			String projectName = MADN3SController.sharedPrefsGetString("project_name");
			JSONObject json = new JSONObject();
	        json.put("action", "photo");
	        json.put("project_name", projectName);
			sendMessageToCameras(json.toString());
		} catch (JSONException e){
            Log.d(tag, "Error armando el JSON");
        } catch (Exception e){
            Log.d(tag, "Error generico enviando");
        }
	}
	public void sendMessageToCameras(String msgString){
		try{
			JSONObject msg = new JSONObject(msgString);
        	if(camera1WeakReference != null){
	        	msg.put("side", "left");
	        	msg.put("camera_name", camera1.getName());
				HiddenMidgetWriter sendCamera1 = new HiddenMidgetWriter(camera1WeakReference, msg.toString());
				sendCamera1.execute();
		        Log.d(tag, "Enviando a Camara1: " + camera1.getName());
		        MADN3SController.readCamera1.set(true);
			} else {
				Log.d(tag, "camera1WeakReference null");
			}
			
			if(camera2WeakReference != null){
				msg.put("side", "right");
				msg.put("camera_name", camera2.getName());
				HiddenMidgetWriter sendCamera2 = new HiddenMidgetWriter(camera2WeakReference, msg.toString());
				sendCamera2.execute();
		        Log.d(tag, "Enviando a Camara2: " + camera2.getName());
		        MADN3SController.readCamera2.set(true);
			} else {
				Log.d(tag, "camera2WeakReference null");
			}
			
			Bundle bundle = new Bundle();
			bundle.putInt("state", org.madn3s.controller.MADN3SController.State.CONNECTING.getState());
			bundle.putInt("device",  Device.CAMERA1.getValue());
			scannerBridge.callback(bundle);
			bundle.putInt("device",  Device.CAMERA2.getValue());
			scannerBridge.callback(bundle);
		} catch (JSONException e){
            Log.d(tag, "Error armando el JSON");
        } catch (Exception e){
            Log.d(tag, "Error generico enviando");
        }
	}
	
	public void processAnswer(String msgString){
		try {
			JSONObject msg = new JSONObject(msgString);
			if(msg.has("error") && !msg.getBoolean("error")){
				int iter = 0;
				JSONObject fr = null;
				iter = MADN3SController.sharedPrefsGetInt("iter");
				fr = MADN3SController.sharedPrefsGetJSONObject("frame-"+iter);
				if(msg.has("side")){
					int device = 1;
					String side = msg.getString("side");
					if(side.equalsIgnoreCase("right")){
						JSONObject rightJson = msg;
						rightJson.remove("side");
						rightJson.remove("time");
						rightJson.remove("error");
						rightJson.remove("camera");
						fr.put("right", rightJson);
						device = Device.CAMERA1.getValue();
					} else if(side.equalsIgnoreCase("left")){
						JSONObject leftJson = msg;
						leftJson.remove("side");
						leftJson.remove("time");
						leftJson.remove("error");
						leftJson.remove("camera");
						fr.put("left", leftJson);
						device = Device.CAMERA2.getValue();
					}
					Bundle bundle = new Bundle();
					bundle.putInt("state", org.madn3s.controller.MADN3SController.State.CONNECTED.getState());
					bundle.putInt("device", device);
					scannerBridge.callback(bundle);
					MADN3SController.sharedPrefsPutJSONObject("frame-"+iter, fr);
					if(fr.has("right") && fr.has("left")){
						iter++;
						int points = MADN3SController.sharedPrefsGetInt("points");
						MADN3SController.sharedPrefsPutInt("iter", iter);
						if(iter != points){
							sendMessageToNXT();
						} else {
							notifyScanFinished();
						}
						Log.d(tag, "iter = " + iter);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void notifyScanFinished() {
		Bundle bundle = new Bundle();
		bundle.putInt("state", org.madn3s.controller.MADN3SController.State.CONNECTED.getState());
		bundle.putInt("device", Device.NXT.getValue());
		scannerBridge.callback(bundle);
		bundle.putInt("device",  Device.CAMERA1.getValue());
		scannerBridge.callback(bundle);
		bundle.putInt("device",  Device.CAMERA2.getValue());
		scannerBridge.callback(bundle);
	}

	private void sendMessageToNXT() {
		Bundle bundle = new Bundle();
		bundle.putInt("state", org.madn3s.controller.MADN3SController.State.CONNECTING.getState());
		bundle.putInt("device", Device.NXT.getValue());
		scannerBridge.callback(bundle);
	}

}
