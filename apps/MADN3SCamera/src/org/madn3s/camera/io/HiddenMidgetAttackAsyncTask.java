package org.madn3s.camera.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetAttackAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String tag = "Awesome AsyncTask";
	private BluetoothServerSocket mBluetoothServerSocket;
    private BluetoothSocket mSocket;
    private Exception ex;
    private final static int SERVER_SOCKET_TIMEOUT = 3000000;

    public HiddenMidgetAttackAsyncTask(BluetoothAdapter mBluetoothAdapter, BluetoothServerSocket mBluetoothServerSocket){
    	this.mBluetoothServerSocket = mBluetoothServerSocket;
    	try {
            mBluetoothServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BTConnection.SERVICE_NAME, BTConnection.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute(){
        Log.d("Camera", "Iniciando task de BT y cosa");
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (true) {
                mSocket = mBluetoothServerSocket.accept(SERVER_SOCKET_TIMEOUT);

                if (mSocket != null){
//                    mBluetoothServerSocket.close();
                    break;
                }
            }
        } catch (Exception e) {
            this.ex = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        //if(e!= null){
            //e.printStackTrace();
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
                
            //poner el socket en algun lado
            }else{
                Log.d(tag, "Conexion fallida");
            }
        //}
        try{
            Log.d(tag, "Lei esto = "+mSocket.getInputStream().read());
        } catch (Exception e){
            Log.d(tag, "FUCK You "+e);
        }
        BTConnection conn = BTConnection.getInstance();
        conn.setControllerSocket(mSocket);
    }

    @Override
    protected void onCancelled(){
        try {
            mBluetoothServerSocket.close();
        } catch (IOException e) {
            this.ex = e;
            e.printStackTrace();
        }
    }
}
