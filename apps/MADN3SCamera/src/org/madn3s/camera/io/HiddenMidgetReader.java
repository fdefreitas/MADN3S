package org.madn3s.camera.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.StaticLayout;
import android.util.Log;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetReader extends HandlerThread implements Callback {
	
	private static String tag = "HiddenMidgetReader";
	private Handler handler, callback;
	private WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference;
    private BluetoothServerSocket mSocket;

	public HiddenMidgetReader(String name, WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference) {
		super(name);
		this.mBluetoothServerSocketWeakReference = mBluetoothServerSocketWeakReference;
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
		String message;
		//TODO cambiar por el otro handle
//		Intent williamWallaceIntent = new Intent(this, BraveheartMidgetService.class);
//		startService(williamWallaceIntent);
		mSocket = mBluetoothServerSocketWeakReference.get();
		while(true){
			message = getMessage();
			if(message != null){
				
			}
		}
	}
	
	private String getMessage(){
		//TODO Move to Service
        Log.d(tag, "Intentando hacer lectura de Socket.\n");
        try{
        	Log.d(tag, "Entrando al Try del InputStreamReader.");
        	ByteArrayOutputStream bao = new ByteArrayOutputStream();
        	bao.reset();
        	InputStream inputStream = mSocket.getInputStream();
        	int b = 0;
        	int threshold = 0;
        	while(true){
        		while (inputStream.available() == 0 && threshold < 3000) { 
                    Thread.sleep(1);
                    threshold++;
                }
        		
        		if(threshold < 3000){
        			threshold = 0;
        			b = inputStream.read();
        			bao.write(b);
            		if(b == 255){
            			break;
            		}
            		Thread.sleep(1);
        		} else {
        			break;
        		}
        	}
        	JSONObject jsonPayload = new JSONObject(bao.toString());
        	Log.d(tag, "Lei esto: " + jsonPayload.toString(1));
        } catch (Exception e){
            Log.d(tag, "Exception al leer Socket: " + e);
            e.printStackTrace();
        }
	}
}
