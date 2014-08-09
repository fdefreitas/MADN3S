package org.madn3s.controller.io;

import android.R.string;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.controller.MADN3SController;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by ninja_midget on 2/1/14.
 */
public class HiddenMidgetAttackAsyncTask extends AsyncTask<Void, Void, Void> {

    private BluetoothSocket mSocket;
    private Exception e;
    private String side;

    public HiddenMidgetAttackAsyncTask(BluetoothDevice mBluetoothDevice){
    	side = "none";
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public HiddenMidgetAttackAsyncTask(BluetoothDevice mBluetoothDevice, String side){
    	this.side = side;
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
            Log.d("AQUI ESTOY", ""+mSocket.isConnected());
            mSocket.connect();
        } catch (Exception e) {
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
        Log.d("Awesome AsyncTask", mSocket.getRemoteDevice().getName() + " " + mSocket.toString());
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
        try{
	        JSONObject json = new JSONObject();
	        json.put("project_name", "first");
			json.put("camera_number", side);
			json.put("camera_name", mSocket.getRemoteDevice().getName());
			sendBytes(json.toString().getBytes());
        } catch (JSONException e){
            Log.d("Awesome AsyncTask", "FUCK YOU JSON");
        } catch (Exception e){
            Log.d("Awesome AsyncTask", "FUCK YOU");
        }
        //poner el socket en algun lado
        BTConnection conn = BTConnection.getInstance();
        conn.setCamSocket(mSocket);
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
    
    public void sendJSON(JSONObject json){
    	OutputStreamWriter osw = null;
    	try{
    		osw = new OutputStreamWriter(mSocket.getOutputStream());
    		String jsonString = json.toString();
    		osw.write(jsonString, 0, jsonString.length());
            Log.d("Awesome AsyncTask", "envie con " + json.toString() );
            osw.flush();
        } catch (Exception e){
            Log.d("Awesome AsyncTask", "FUCK YOU sendJSON");
        } finally {
        	try{osw.close();} catch (Exception e){}
        }
    }
    
    public void sendBytes(byte[] json){
    	try{
    		OutputStream os = mSocket.getOutputStream();
    		os.flush();
    		os.write(json);
    		Log.d("Awesome AsyncTask", "envie con " + json.toString() );
        } catch (Exception e){
            Log.d("Awesome AsyncTask", "FUCK YOU sendBytes");
        } 
    }
}
