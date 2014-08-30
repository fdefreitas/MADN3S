package org.madn3s.controller.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;

public class HiddenMidgetReader extends HandlerThread implements Callback {
	public static UniversalComms bridge;
	private final static String tag = "HiddenMidgetReader";
	public final static String EXTRA_CALLBACK_MSG = "message";
	private Handler handler, callback;
	private WeakReference<BluetoothSocket> mBluetoothSocketWeakReference;
    private BluetoothSocket mSocket;
    private AtomicBoolean read;

	public HiddenMidgetReader(String name, WeakReference<BluetoothSocket> mBluetoothSocketWeakReference) {
		super(name);
		this.mBluetoothSocketWeakReference = mBluetoothSocketWeakReference;
	}
	
	public HiddenMidgetReader(String name, WeakReference<BluetoothSocket> mBluetoothSocketWeakReference, AtomicBoolean read) {
		super(name);
		this.mBluetoothSocketWeakReference = mBluetoothSocketWeakReference;
		this.read = read;
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
			while(MADN3SController.isRunning.get()){
				if(read.get()){
					Log.d(tag, "Esperando mensaje de " + mSocket.getRemoteDevice().getName());
					message = getMessage();
					if(message != null && !message.isEmpty()){
						Log.d(tag, "Llego " + message + " de " + mSocket.getRemoteDevice().getName());
						JSONObject msg = new JSONObject(message);
						if(msg.has("action")){
							String action = msg.getString("action");
							 if(action.equalsIgnoreCase("exit_app")){
								break;
							}
						}	
//							bridge.callback(message);
//							Log.d(tag, "Iniciando wait().");
//							MADN3SController.isPictureTaken.set(false);
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