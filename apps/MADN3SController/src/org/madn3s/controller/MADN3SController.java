package org.madn3s.controller;

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
	
	public Handler getBluetoothHandler() {
		return mBluetoothHandler;
	}
	
	public void setBluetoothHandlerCallBack(Handler.Callback callback) {
	    this.mBluetoothHandlerCallback = callback;
	}
}
