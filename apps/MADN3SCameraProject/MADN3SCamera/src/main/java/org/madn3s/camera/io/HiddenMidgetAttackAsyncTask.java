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

    private BluetoothServerSocket mBluetoothServerSocket;
    private BluetoothSocket mSocket;
    private Exception e;
    private final static int SERVER_SOCKET_TIMEOUT = 3000000;

    public HiddenMidgetAttackAsyncTask(BluetoothAdapter mBluetoothAdapter){
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
            this.e = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        //if(e!= null){
            //e.printStackTrace();
            if(mSocket != null){
                Log.d("Awesome AsyncTask", "Conexion levantada");
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
            }else{
                Log.d("Awesome AsyncTask", "Conexion fallida");
            }
        //}
        try{
            Log.d("Awesome AsyncTask", ""+mSocket.getInputStream().read());
        } catch (Exception e){
            Log.d("Awesome AsyncTask", "FUCK You"+e);
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
