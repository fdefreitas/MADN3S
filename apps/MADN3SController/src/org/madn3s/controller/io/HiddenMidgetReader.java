package org.madn3s.controller.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;
import org.madn3s.controller.MADN3SController.Device;
import org.madn3s.controller.fragments.ConnectionFragment;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;

public class HiddenMidgetReader extends HandlerThread implements Callback {
	public static UniversalComms bridge;
	public static UniversalComms connectionFragmentBridge;
	private final static String tag = "HiddenMidgetReader";
	public final static String EXTRA_CALLBACK_MSG = "message";
	public final static String EXTRA_CALLBACK_SEND = "send";
	public final static String EXTRA_CALLBACK_NXT_MESSAGE = "nxt_message";
	private Handler handler, callback;
	private WeakReference<BluetoothSocket> mBluetoothSocketWeakReference;
    private BluetoothSocket mSocket;
    private AtomicBoolean read;
    private String side;

	public HiddenMidgetReader(String name, WeakReference<BluetoothSocket> mBluetoothSocketWeakReference) {
		super(name);
		this.mBluetoothSocketWeakReference = mBluetoothSocketWeakReference;
		this.side = "DEFAULT";
	}
	
	public HiddenMidgetReader(String name, WeakReference<BluetoothSocket> mBluetoothSocketWeakReference, AtomicBoolean read, String side) {
		super(name);
		this.mBluetoothSocketWeakReference = mBluetoothSocketWeakReference;
		this.read = read;
		this.side = side;
	}
	
	public HiddenMidgetReader(String name, int priority) {
		super(name, priority);
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	@Override
	protected void onLooperPrepared() {
		handler =  new Handler(getLooper(), this);
	}

	@Override
	public void run() {
		try {
			String message;
			while (true) {
				if (mBluetoothSocketWeakReference != null){
					mSocket = mBluetoothSocketWeakReference.get();
					break;
				}
	        }
			
			int bondState = mSocket.getRemoteDevice().getBondState();
			boolean isConnected = mSocket.isConnected();
			int state, device;
			Bundle bundle = new Bundle();
			
			if(isConnected){
				switch (bondState){
		            case BluetoothDevice.BOND_BONDED:
		            	state = org.madn3s.controller.MADN3SController.State.CONNECTED.getState();
		                break;
		            case BluetoothDevice.BOND_BONDING:
		            	state = org.madn3s.controller.MADN3SController.State.CONNECTING.getState();
		                break;
		            default:
		            case BluetoothDevice.BOND_NONE:
		            	state = org.madn3s.controller.MADN3SController.State.FAILED.getState();
		                break;
		        }
			} else {
				state = org.madn3s.controller.MADN3SController.State.FAILED.getState();
			}
			
			if(MADN3SController.isCamera1(mSocket.getRemoteDevice().getAddress())){
	        	device = Device.CAMERA1.getValue();
	        } else if(MADN3SController.isCamera2(mSocket.getRemoteDevice().getAddress())){
	        	device = Device.CAMERA2.getValue();
	        } else {
	        	device = Device.NXT.getValue();
	        }

			bundle.putInt("state", state);
			bundle.putInt("device", device);
			long start = 0;
			connectionFragmentBridge.callback(bundle);
			if(state == org.madn3s.controller.MADN3SController.State.CONNECTED.getState()){
				while(MADN3SController.isRunning.get()){
					if(read.get()){
						if(start == 0){
							start = System.currentTimeMillis();
						}
						message = getMessage();
						if(message != null && !message.isEmpty()){
//							Log.d(tag, "Llego " + message + " de " + mSocket.getRemoteDevice().getName() + " en " + (System.currentTimeMillis() - start));
							JSONObject msg = new JSONObject(message);
							if(msg.has("action")){
								String action = msg.getString("action");
								 if(action.equalsIgnoreCase("exit_app")){
									break;
								}
							}
							msg.put("camera", mSocket.getRemoteDevice().getName());
							msg.put("side", side);
							msg.put("time", System.currentTimeMillis() - start);
							bridge.callback(msg.toString());
							start = 0;
							read.set(false);
						}
					}
				}
			}
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	}
	
	private String getMessage(){
		try{
        	int byteTemp = 0;
        	int threshold = 0;
        	ByteArrayOutputStream bao = new ByteArrayOutputStream();
        	bao.reset();
        	InputStream inputStream = mSocket.getInputStream();
        	while(true){
        		while (inputStream.available() == 0 && threshold < 3000) { 
                    Thread.sleep(1);
                    threshold++;
                }
        		
        		if(threshold < 3000){
        			threshold = 0;
        			byteTemp = inputStream.read();
        			bao.write(byteTemp);
            		if(byteTemp == 255){
            			break;
            		}
            		Thread.sleep(1);
        		} else {
        			break;
        		}
        	}
        	return bao != null ? bao.toString() : null;
        } catch (Exception e){
            Log.d(tag, "getMessage. Exception al leer Socket: " + e);
            e.printStackTrace();
            return null;
        }
	}
}