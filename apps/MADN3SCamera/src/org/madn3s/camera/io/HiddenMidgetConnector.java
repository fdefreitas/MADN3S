package org.madn3s.camera.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.json.JSONObject;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetConnector extends AsyncTask<Void, Void, Void> {

    private static final String tag = "HiddenMidgetConnector";
	private BluetoothServerSocket mBluetoothServerSocket;
	private WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference;
	private WeakReference<BluetoothSocket> mSocketWeakReference;
    private BluetoothSocket mSocket;
    private Exception e;
    private final static int SERVER_SOCKET_TIMEOUT = 3000000;

    public HiddenMidgetConnector(WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference, WeakReference<BluetoothSocket> mSocketWeakReference){
    	this.mBluetoothServerSocket = mBluetoothServerSocketWeakReference.get();
    	this.mSocketWeakReference = mSocketWeakReference;
    }

    @Override
    protected void onPreExecute(){
        Log.d(tag, tag + " PreExecute");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
        	int timeout = 0;
            while (timeout < SERVER_SOCKET_TIMEOUT) {
                mSocket = mBluetoothServerSocket.accept(SERVER_SOCKET_TIMEOUT);

                if (mSocket != null){
	                mBluetoothServerSocket.close();
	                break;
                }
                Log.d(tag, "Esperando conexión... " + timeout);
                Thread.sleep(5);
                timeout++;
            }
        } catch (Exception e) {
            this.e = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        if(e == null){
            if(mSocket != null){
                Log.d(tag, "Conexion levantada");
                if (mSocket.getRemoteDevice()!= null)
                    Log.d(tag, mSocket.getRemoteDevice().getName());
                switch (mSocket.getRemoteDevice().getBondState()){
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(tag, "BOND_BONDED");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(tag, "BOND_BONDING");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(tag, "BOND_NONE");
                        break;
                    default:
                        Log.d(tag, "Default");
                }
                                
                mSocketWeakReference = new WeakReference<BluetoothSocket>(mSocket);
                Log.d(tag, "mSocketWeakReference.get(): " + mSocketWeakReference.get().toString());
                
                HiddenMidgetReader readerHandlerThreadThread = new HiddenMidgetReader("readerTask", mSocketWeakReference);
	            Log.d(tag, "Ejecutando a HiddenMidgetReader");
	            readerHandlerThreadThread.start();
                
            }else{
                Log.d(tag, "Conexion fallida");
            }
        } else {
        	Log.d(tag, "Error abriendo Socket.");
        }
    }

    @Override
    protected void onCancelled(){
        try {
            mBluetoothServerSocket.close();
        } catch (IOException e) {
            this.e = e;
            e.printStackTrace();
        }
    }
}