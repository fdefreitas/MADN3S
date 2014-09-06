package org.madn3s.controller.io;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.madn3s.controller.MADN3SController;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

public class HiddenMidgetConnector extends AsyncTask<Void, Void, Void> {
	
	private static final String tag = "HiddenMidgetConnector";
	private WeakReference<BluetoothSocket> mSocketWeakReference;
    private BluetoothSocket mSocket;
    private Exception e;
    private AtomicBoolean read;
    
    public HiddenMidgetConnector(BluetoothDevice mBluetoothDevice, WeakReference<BluetoothSocket> mSocketWeakReference){
    	this.mSocketWeakReference = mSocketWeakReference;
    	try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.read = new AtomicBoolean(false);
    }
    
    public HiddenMidgetConnector(BluetoothDevice mBluetoothDevice, WeakReference<BluetoothSocket> mSocketWeakReference, AtomicBoolean read){
    	this.mSocketWeakReference = mSocketWeakReference;
    	try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.read = read;
    }

	@Override
	protected Void doInBackground(Void... params) {
		 try {
            Log.d(tag, ""+mSocket.isConnected() + " - " + mSocket.getRemoteDevice().getName());
            mSocket.connect();
        } catch (Exception e) {
            this.e = e;
            try {
                mSocket.close();
            } catch (IOException ex) {
                this.e = ex;
            }
        }
        return null;
	}
	
	@Override
    protected void onPostExecute(Void result){
        if (e!= null) e.printStackTrace();
        Log.d(tag, mSocket.getRemoteDevice().getName() + " " + mSocket.toString());
        if(mSocket.isConnected()){
            Log.d(tag, "Conexion levantada " + mSocket.getRemoteDevice().getName());
        }else{
            Log.d(tag, "Conexion fallida " + mSocket.getRemoteDevice().getName());
		}

        if (mSocket.getRemoteDevice()!= null){
            Log.d(tag, mSocket.getRemoteDevice().getName());
        }
        
        switch (mSocket.getRemoteDevice().getBondState()){
            case BluetoothDevice.BOND_BONDED:
                Log.d(tag, "BOND_BONDED - " + mSocket.getRemoteDevice().getName());
                break;
            case BluetoothDevice.BOND_BONDING:
                Log.d(tag, "BOND_BONDING - " + mSocket.getRemoteDevice().getName());
                break;
            case BluetoothDevice.BOND_NONE:
                Log.d(tag, "BOND_NONE - " + mSocket.getRemoteDevice().getName());
                break;
            default:
                Log.d(tag, "Default - " + mSocket.getRemoteDevice().getName());
        }
//        mSocketWeakReference = new WeakReference<BluetoothSocket>(mSocket);
        WeakReference<BluetoothSocket> mSocketWeakReference = new WeakReference<BluetoothSocket>(mSocket);
        if(MADN3SController.isCamera1(mSocket.getRemoteDevice().getAddress())){
        	MADN3SController.camera1WeakReference = mSocketWeakReference;
        	
        } else if(MADN3SController.isCamera2(mSocket.getRemoteDevice().getAddress())){
        	MADN3SController.camera2WeakReference = mSocketWeakReference;
        } else {
        	Log.d(tag, "WHUT?!");
        }
        HiddenMidgetReader readerHandlerThread = new HiddenMidgetReader("readerTask-" + mSocket.getRemoteDevice().getName(), mSocketWeakReference, read);
        Log.d(tag, "Ejecutando a HiddenMidgetReader");
        readerHandlerThread.start();
    }

    @Override
    protected void onCancelled(){
        try {
            mSocket.close();
        } catch (IOException e) {
            this.e = e;
            e.printStackTrace();
        }
    }

}
