package org.madn3s.camera.io;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.madn3s.camera.Consts;


/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetWriter extends AsyncTask<Void, Void, Void> {

    private static final String tag = HiddenMidgetWriter.class.getSimpleName();
	private BluetoothSocket mSocket;
    private Exception e;
    private byte[] msg;
    
    public HiddenMidgetWriter(WeakReference<BluetoothSocket> mBluetoothSocketWeakReference, Object msg){
    	mSocket = mBluetoothSocketWeakReference.get();
    	if(msg instanceof String){
    		this.msg = ((String) msg).getBytes();
    	} else if(msg instanceof Bitmap){
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		((Bitmap) msg).compress(Consts.BITMAP_COMPRESS_FORMAT, Consts.COMPRESSION_QUALITY, baos);
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
        	Log.d(tag, "Mensaje: " + msg + " enviado a " + mSocket.getRemoteDevice().getName());
        } else {
        	Log.d(tag, "Ocurrio un error enviando mensaje: " + msg + " a " + mSocket.getRemoteDevice().getName());
        }
    }

    
}
