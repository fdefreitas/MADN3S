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

    private static final String tag = "HiddenMidgetAttackAsyncTask";
	private BluetoothServerSocket mBluetoothServerSocket;
	private WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference;
    private BluetoothSocket mSocket;
    private Exception e;
    private final static int SERVER_SOCKET_TIMEOUT = 3000000;

    public HiddenMidgetConnector(WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference){
    	this.mBluetoothServerSocket = mBluetoothServerSocketWeakReference.get();
    }

    @Override
    protected void onPreExecute(){
        Log.d(tag, "Iniciando task de BT y cosa");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (true) {
                mSocket = mBluetoothServerSocket.accept(SERVER_SOCKET_TIMEOUT);

                if (mSocket != null){
                	//TODO creo que no es necesario, si cierra la conexion no se puede seguir escuchando
	                mBluetoothServerSocket.close();
	                break;
                }
            }
        } catch (Exception e) {
            this.e = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        if(e!= null){
            e.printStackTrace();
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
                
            //TODO poner el socket en algun lado
            }else{
                Log.d(tag, "Conexion fallida");
            }
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
