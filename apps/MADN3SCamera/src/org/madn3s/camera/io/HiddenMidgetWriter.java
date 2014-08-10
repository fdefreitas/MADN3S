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
import org.json.JSONObject;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetWriter extends AsyncTask<Void, Void, Void> {

    private static final String tag = "HiddenMidgetAttackAsyncTask";
	private BluetoothServerSocket mBluetoothServerSocket;
    private BluetoothSocket mSocket;
    private Exception e;
    private final static int SERVER_SOCKET_TIMEOUT = 3000000;

    public HiddenMidgetWriter(BluetoothAdapter mBluetoothAdapter, BluetoothServerSocket mBluetoothServerSocket){
    	this.mBluetoothServerSocket = mBluetoothServerSocket;
    	try {
            this.mBluetoothServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BTConnection.SERVICE_NAME, BTConnection.APP_UUID);
        } catch (IOException e) {
        	Log.d(tag, "No se pudo inicializar mBluetoothServerSocket. Imprimiendo Stack Trace:");
            e.printStackTrace();
        }
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
        
        //TODO Move to Service
        Log.d(tag, "Intentando hacer lectura de Socket.\n");
        try{
        	Log.d(tag, "Entrando al Try del InputStreamReader.");
//        	InputStreamReader isr = new InputStreamReader(mSocket.getInputStream());
//        	StringWriter writer = new StringWriter();
//        	IOUtils.copy(isr, writer);
//            Log.d(tag, "Lei esto: " + writer.toString());
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
