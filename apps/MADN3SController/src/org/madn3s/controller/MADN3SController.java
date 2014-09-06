package org.madn3s.controller;

import android.R.integer;
import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by inaki on 1/11/14.
 */
public class MADN3SController extends Application {
	public static final String SERVICE_NAME ="MADN3S";
    public static final UUID APP_UUID = UUID.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
    
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
    	SCANNER("SCANNER",0),
    	CONTROLLER("CONTROLLER",1);

    	private String strVal;
    	private int intVal;
    	
    	Mode(String strVal, int intVal){
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
    	NXT("NXT",0),
    	CAMERA1("CAMERA1",1),
    	CAMERA2("CAMERA2",2);
    	
    	private String strVal;
    	private int intVal;
    	
    	Device(String strVal, int intVal){
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
    	
    	
    	public static Device setDevice(int device){
			switch (device){
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
    	CONNECTED(0),
    	CONNECTING(1),
    	FAILED(2);
    	
    	private int state;
    	
    	State(int state){
    		this.state = state;
    	}
    	
    	public int getState() {
            return state;
    	}
    	
		@Override
		public String toString() {
			return "state: " + this.state;
		}
		
		public static State setState(int state){
			switch (state){
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
		// TODO Auto-generated method stub
		super.onCreate();
		mBluetoothHandler = new Handler() {
    	    public void handleMessage(android.os.Message msg) {
    	        if (mBluetoothHandlerCallback != null) {
    	            mBluetoothHandlerCallback.handleMessage(msg);
    	        }
    	    };
    	};
	}

	public static boolean isToyDevice(BluetoothDevice device){
		return device.getBluetoothClass()!= null && device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT;
	}

	public static boolean isCameraDevice(BluetoothDevice device){
		return device.getBluetoothClass()!= null && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART || device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.Major.MISC);
	}
	
	public static boolean isCamera1(String macAddress){
		if(macAddress != null  && camera1 != null && camera1.getAddress() != null){
			return macAddress.equalsIgnoreCase(camera1.getAddress());
		}
		return false;
	}
	
	public static boolean isCamera2(String macAddress){
		if(macAddress != null  && camera2 != null && camera2.getAddress() != null){
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
