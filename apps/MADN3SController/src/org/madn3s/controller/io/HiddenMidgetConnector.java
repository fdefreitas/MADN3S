package org.madn3s.controller.io;

import java.io.IOException;
import java.lang.ref.WeakReference;

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
    
    public HiddenMidgetConnector(BluetoothDevice mBluetoothDevice, WeakReference<BluetoothSocket> mSocketWeakReference){
    	this.mSocketWeakReference = mSocketWeakReference;
    	try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	protected Void doInBackground(Void... params) {
		 try {
            Log.d(tag, ""+mSocket.isConnected());
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
            Log.d(tag, "Conexion levantada");
        }else{
            Log.d(tag, "Conexion fallida");
		}

        if (mSocket.getRemoteDevice()!= null){
            Log.d(tag, mSocket.getRemoteDevice().getName());
        }
        
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
