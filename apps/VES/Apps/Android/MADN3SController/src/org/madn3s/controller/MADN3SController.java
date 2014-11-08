package org.madn3s.controller;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.controller.components.NXTTalker;
import org.madn3s.controller.ves.KiwiNative;

import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

/**
 * Created by inaki on 1/11/14.
 */
public class MADN3SController extends Application {
	private static final String tag = "MADN3SController";
	public static final String MODEL_MESSAGE = "MODEL";
	public static final String SERVICE_NAME = "MADN3S";
	public static final UUID APP_UUID = UUID
			.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
	
	public static final String defaultJSONObjectString = "{}";
	public static final String defaultJSONArrayString = "[]";

	public static SharedPreferences sharedPreferences;
	public static Editor sharedPreferencesEditor;
	public static BluetoothDevice nxt;
	public static BluetoothDevice rightCamera;
	public static BluetoothDevice leftCamera;

	private Handler mBluetoothHandler;
	private Handler.Callback mBluetoothHandlerCallback = null;

	public static WeakReference<BluetoothSocket> rightCameraWeakReference = null;
	public static WeakReference<BluetoothSocket> leftCameraWeakReference = null;

	public static AtomicBoolean isPictureTaken;
	public static AtomicBoolean isRunning;

	public static AtomicBoolean readRightCamera;
	public static AtomicBoolean readLeftCamera;
	
	public static NXTTalker talker;
	public static boolean isOpenCvLoaded;

	public static enum Mode {
		SCANNER("SCANNER", 0), CONTROLLER("CONTROLLER", 1), SCAN("SCAN", 2);

		private String strVal;
		private int intVal;

		Mode(String strVal, int intVal) {
			this.strVal = strVal;
			this.intVal = intVal;
		}

		public int getValue() {
			return intVal;
		}

		@Override
		public String toString() {
			return this.strVal;
		}
	}

	public static enum Device {
		NXT("NXT", 0), RIGHT_CAMERA("RIGHT_CAMERA", 1), LEFT_CAMERA("LEFT_CAMERA", 2);

		private String strVal;
		private int intVal;

		Device(String strVal, int intVal) {
			this.strVal = strVal;
			this.intVal = intVal;
		}

		public int getValue() {
			return intVal;
		}

		@Override
		public String toString() {
			return this.strVal;
		}

		public static Device setDevice(int device) {
			switch (device) {
			case 0:
				return NXT;
			case 1:
				return RIGHT_CAMERA;
			default:
			case 2:
				return LEFT_CAMERA;
			}
		}
	}

	public static enum State {
		CONNECTED(0), CONNECTING(1), FAILED(2);

		private int state;

		State(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}

		@Override
		public String toString() {
			return "state: " + this.state;
		}

		public static State setState(int state) {
			switch (state) {
			case 0:
				return CONNECTED;
			case 1:
				return CONNECTING;
			default:
			case 2:
				return FAILED;
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setSharedPreferences();
		Log.d(tag, "MADN3SController onCreate()");
		
		MADN3SController.isPictureTaken = new AtomicBoolean(true);
        MADN3SController.isRunning = new AtomicBoolean(true);
        MADN3SController.readRightCamera = new AtomicBoolean(false);
        MADN3SController.readLeftCamera = new AtomicBoolean(false);
		
		mBluetoothHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (mBluetoothHandlerCallback != null) {
					mBluetoothHandlerCallback.handleMessage(msg);
				}
			};
		};
	}
	
	
	/**
	 * Sets SharedPreferences and SharedPreferences Editor for later use with methods defined further
	 */
	private void setSharedPreferences() {
		sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
		sharedPreferencesEditor = MADN3SController.sharedPreferences.edit();
	}
	
	public static void clearSharedPreferences() {
		sharedPreferencesEditor.clear().apply();
	}
	
	public static void removeKeyFromSharedPreferences(String key) {
		sharedPreferencesEditor.remove(key).apply();
	}
	
	public static void sharedPrefsPutJSONArray(String key, JSONArray value){
		sharedPreferencesEditor.putString(key, value.toString()).apply();
	}
	
	public static JSONArray sharedPrefsGetJSONArray(String key){
		String jsonString = sharedPreferences.getString(key, defaultJSONArrayString);
		try {
			return new JSONArray(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}
	
	public static void sharedPrefsPutJSONObject(String key, JSONObject value){
		sharedPreferencesEditor.putString(key, value.toString()).apply();
	}
	
	public static JSONObject sharedPrefsGetJSONObject(String key){
		String jsonString = sharedPreferences.getString(key, defaultJSONObjectString);
		try {
			return new JSONObject(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}
	
	public static void sharedPrefsPutString(String key, String value){
		sharedPreferencesEditor.putString(key, value).apply();
	}
	
	public static String sharedPrefsGetString(String key){
		return sharedPreferences.getString(key, "");
	}
	
	public static void sharedPrefsPutBoolean(String key, Boolean value){
		sharedPreferencesEditor.putBoolean(key, value).apply();
	}
	
	public static Boolean sharedPrefsGetBoolean(String key){
		return sharedPreferences.getBoolean(key, false);
	}
	
	public static void sharedPrefsPutInt(String key, int value){
		sharedPreferencesEditor.putInt(key, value).apply();
	}
	
	public static int sharedPrefsGetInt(String key){
		return sharedPreferences.getInt(key, 0);
	}
	
	public static void sharedPrefsPutLong(String key, Long value){
		sharedPreferencesEditor.putLong(key, value).apply();
	}
	
	public static Long sharedPrefsGetLong(String key){
		return sharedPreferences.getLong(key, (long) 0);
	}
	
	public static void sharedPrefsPutFloat(String key, Float value){
		sharedPreferencesEditor.putFloat(key, value).apply();
	}
	
	public static Float sharedPrefsGetFloat(String key){
		return sharedPreferences.getFloat(key, 0);
	}

	public static boolean isToyDevice(BluetoothDevice device) {
		return device.getBluetoothClass() != null
				&& device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT;
	}

	public static boolean isCameraDevice(BluetoothDevice device) {
		return device.getBluetoothClass() != null
				&& (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART || device
						.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.Major.MISC);
	}

	public static boolean isRightCamera(String macAddress) {
		if (macAddress != null && rightCamera != null
				&& rightCamera.getAddress() != null) {
			return macAddress.equalsIgnoreCase(rightCamera.getAddress());
		}
		return false;
	}

	public static boolean isLeftCamera(String macAddress) {
		if (macAddress != null && leftCamera != null
				&& leftCamera.getAddress() != null) {
			return macAddress.equalsIgnoreCase(leftCamera.getAddress());
		}
		return false;
	}

	public Handler getBluetoothHandler() {
		return mBluetoothHandler;
	}

	public void setBluetoothHandlerCallBack(Handler.Callback callback) {
		this.mBluetoothHandlerCallback = callback;
	}
	
	public static void pointsTest(){
		int points = MADN3SController.sharedPrefsGetInt("points");
		JSONArray framesJson = new JSONArray();
		JSONObject pointsJson = new JSONObject();
		for(int i = 0; i < points; i++){
			JSONObject frame = MADN3SController.sharedPrefsGetJSONObject("frame-"+i);
			framesJson.put(frame);
//			Log.d(tag, "frame-"+i + " = " + frame.toString());
		}
		
		try {
			pointsJson.put("name", MADN3SController.sharedPrefsGetString("project_name"));
			pointsJson.put("pictures", framesJson);
			Log.d(tag, "pointTest.pointsJson String length: " + pointsJson.toString().length());
			KiwiNative.doProcess(pointsJson.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(tag, "generateModelButton.OnClick. Error composing points JSONObject");
		}
	}
}
