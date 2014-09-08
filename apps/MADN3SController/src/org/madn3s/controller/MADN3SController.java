package org.madn3s.controller;

import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by inaki on 1/11/14.
 */
public class MADN3SController extends Application {
	public static final String SERVICE_NAME = "MADN3S";
	public static final UUID APP_UUID = UUID
			.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
	
	public static final String defaultJSONObjectString = "{}";
	public static final String defaultJSONArrayString = "[]";

	private static SharedPreferences sharedPreferences;
	private static Editor sharedPreferencesEditor;
	public static BluetoothDevice nxt;
	public static BluetoothDevice camera1;
	public static BluetoothDevice camera2;

	private Handler mBluetoothHandler;
	private Handler.Callback mBluetoothHandlerCallback = null;

	public static WeakReference<BluetoothSocket> camera1WeakReference = null;
	public static WeakReference<BluetoothSocket> camera2WeakReference = null;

	public static AtomicBoolean isPictureTaken;
	public static AtomicBoolean isRunning;

	public static AtomicBoolean readCamera1;
	public static AtomicBoolean readCamera2;

	public static enum Mode {
		SCANNER("SCANNER", 0), CONTROLLER("CONTROLLER", 1);

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
		NXT("NXT", 0), CAMERA1("CAMERA1", 1), CAMERA2("CAMERA2", 2);

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
				return CAMERA1;
			default:
			case 2:
				return CAMERA2;
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
		sharedPreferences = getSharedPreferences(getString(R.string.app_name),
				MODE_PRIVATE);
		sharedPreferencesEditor = MADN3SController.sharedPreferences.edit();
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

	public static boolean isToyDevice(BluetoothDevice device) {
		return device.getBluetoothClass() != null
				&& device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT;
	}

	public static boolean isCameraDevice(BluetoothDevice device) {
		return device.getBluetoothClass() != null
				&& (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART || device
						.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.Major.MISC);
	}

	public static boolean isCamera1(String macAddress) {
		if (macAddress != null && camera1 != null
				&& camera1.getAddress() != null) {
			return macAddress.equalsIgnoreCase(camera1.getAddress());
		}
		return false;
	}

	public static boolean isCamera2(String macAddress) {
		if (macAddress != null && camera2 != null
				&& camera2.getAddress() != null) {
			return macAddress.equalsIgnoreCase(camera2.getAddress());
		}
		return false;
	}

	public Handler getBluetoothHandler() {
		return mBluetoothHandler;
	}

	public void setBluetoothHandlerCallBack(Handler.Callback callback) {
		this.mBluetoothHandlerCallback = callback;
	}
}
