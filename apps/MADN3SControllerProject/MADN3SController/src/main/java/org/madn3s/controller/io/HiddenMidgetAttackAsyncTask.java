package org.madn3s.controller.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import org.madn3s.controller.MADN3SController;

import java.io.IOException;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetAttackAsyncTask extends AsyncTask<Void, Void, Void> {

    private BluetoothSocket mSocket;
    private Exception e;

    public HiddenMidgetAttackAsyncTask(BluetoothDevice mBluetoothDevice){
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute(){

    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mSocket.connect();
        } catch (IOException e) {
            // Unable to connect; close the socket and get out
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
        if(mSocket.isConnected())
            Log.d("Awesome AsyncTask", "Conexion levantada");
        else
            Log.d("Awesome AsyncTask", "Conexion fallida");

        if (mSocket.getRemoteDevice()!= null)
            Log.d("Awesome AsyncTask", mSocket.getRemoteDevice().getName());
        switch (mSocket.getRemoteDevice().getBondState()){
            case BluetoothDevice.BOND_BONDED:
                Log.d("Awesome AsyncTask", "BOND_BONDED");
                break;
            case BluetoothDevice.BOND_BONDING:
                Log.d("Awesome AsyncTask", "BOND_BONDING");
                break;
            case BluetoothDevice.BOND_NONE:
                Log.d("Awesome AsyncTask", "BOND_NONE");
                break;
            default:
                Log.d("Awesome AsyncTask", "Default");
        }

        //poner el socket en algun lado
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
