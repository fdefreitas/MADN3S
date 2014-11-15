package org.madn3s.camera.io;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;


/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetWriter extends AsyncTask<Void, Void, Void> {

    private static final String tag = "HiddenMidgetWriter";
	private BluetoothSocket mSocket;
    private Exception e;
    private byte[] msg;
    
    public HiddenMidgetWriter(WeakReference<BluetoothSocket> mBluetoothSocketWeakReference, Object msg){
    	mSocket = mBluetoothSocketWeakReference.get();
    	if(msg instanceof String){
    		this.msg = ((String) msg).getBytes();
    	} else if(msg instanceof Bitmap){
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		//TODO convertir a constante compressformat
    		((Bitmap) msg).compress(Bitmap.CompressFormat.JPEG, 100, baos);
    		this.msg = baos.toByteArray();
    	}
    }

    @Override
    protected Void doInBackground(Void... params) {
    	try{
    		OutputStream os = mSocket.getOutputStream();
    		os.flush();
    		os.write(msg);
    		this.e = null;
        } catch (Exception e){
        	this.e = e;
        	e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        if(e != null){
        	Log.d(tag, "envie " + msg + " a " + mSocket.getRemoteDevice().getName());
        } else {
        	Log.d(tag, "Ocurrio un error enviando " + msg + " a " + mSocket.getRemoteDevice().getName());
        }
    }

    
}
