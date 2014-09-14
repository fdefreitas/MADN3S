package org.madn3s.controller.io;

import static org.madn3s.controller.MADN3SController.camera1;
import static org.madn3s.controller.MADN3SController.camera1WeakReference;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
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
    private String side;
    
    public HiddenMidgetConnector(BluetoothDevice mBluetoothDevice, WeakReference<BluetoothSocket> mSocketWeakReference){
    	this.mSocketWeakReference = mSocketWeakReference;
    	try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.read = new AtomicBoolean(false);
    	this.side = "DEFAULT";
    }
    
    public HiddenMidgetConnector(BluetoothDevice mBluetoothDevice, WeakReference<BluetoothSocket> mSocketWeakReference, AtomicBoolean read, String side){
    	this.mSocketWeakReference = mSocketWeakReference;
    	try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MADN3SController.APP_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	this.read = read;
    	this.side = side;
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
                WeakReference<BluetoothSocket> mSocketWeakReference = new WeakReference<BluetoothSocket>(mSocket);
                String side = "left";
                if(MADN3SController.isCamera1(mSocket.getRemoteDevice().getAddress())){
                	side = "left";
                	MADN3SController.camera1WeakReference = mSocketWeakReference;
                } else if(MADN3SController.isCamera2(mSocket.getRemoteDevice().getAddress())){
                	side = "right";
                	MADN3SController.camera2WeakReference = mSocketWeakReference;
                } else {
                	Log.d(tag, "WHUT?!");
                }
                HiddenMidgetReader readerHandlerThread = new HiddenMidgetReader("readerTask-" + side + "-" + mSocket.getRemoteDevice().getName(), mSocketWeakReference, read, side);
                readerHandlerThread.start();
                sendConfigs(mSocketWeakReference, side, mSocket.getRemoteDevice().getName());
                Log.d(tag, "Ejecutando a HiddenMidgetReader");
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
    
    private void sendConfigs(WeakReference<BluetoothSocket> cameraWeakReference, String side, String name) {
		try{
			JSONObject json = new JSONObject();
	        json.put("action", "config");
	        json.put("clean", MADN3SController.sharedPrefsGetBoolean("clean"));
	        json.put("side", side);
	        json.put("camera_name", name);
	        JSONObject grabCut = new JSONObject();
	        JSONObject rectangle = new JSONObject();
	        JSONObject point1 = new JSONObject();
	        point1.put("x", MADN3SController.sharedPrefsGetInt("p1x"));
	        point1.put("y", MADN3SController.sharedPrefsGetInt("p1y"));
	        rectangle.put("point_1", point1);
	        JSONObject point2 = new JSONObject();
	        point2.put("x", MADN3SController.sharedPrefsGetInt("p2x"));
	        point2.put("y", MADN3SController.sharedPrefsGetInt("p2y"));
	        rectangle.put("point_2", point2);
	        grabCut.put("rectangle", rectangle);
	        grabCut.put("iterations", MADN3SController.sharedPrefsGetInt("iterations"));
	        json.put("grab_cut", grabCut);
	        
	        JSONObject goodFeatures = new JSONObject();
	        goodFeatures.put("max_corners", MADN3SController.sharedPrefsGetInt("maxCorners"));
	        goodFeatures.put("quality_level", MADN3SController.sharedPrefsGetFloat("qualityLevel"));
	        goodFeatures.put("min_distance", MADN3SController.sharedPrefsGetInt("minDistance"));
	        json.put("good_features", goodFeatures);
	        
	        JSONObject edgeDetection = new JSONObject();
	        edgeDetection.put("algorithm", MADN3SController.sharedPrefsGetString("algorithm"));
	        edgeDetection.put("algorithm_index", MADN3SController.sharedPrefsGetInt("algorithmIndex"));
	        JSONObject canny = new JSONObject();
	        canny.put("lower_threshold", MADN3SController.sharedPrefsGetFloat("lowerThreshold"));
	        canny.put("upper_threshold", MADN3SController.sharedPrefsGetFloat("upperThreshold"));
	        edgeDetection.put("canny_config", canny);
	        JSONObject sobel = new JSONObject();
	        sobel.put("d_depth", MADN3SController.sharedPrefsGetInt("dDepth"));
	        sobel.put("d_x", MADN3SController.sharedPrefsGetInt("dX"));
	        sobel.put("d_y", MADN3SController.sharedPrefsGetInt("dY"));
	        edgeDetection.put("sobel_config", sobel);
	        json.put("edge_detection", edgeDetection);
	        HiddenMidgetWriter sendCamera = new HiddenMidgetWriter(cameraWeakReference, json.toString());
	        sendCamera.execute();
	    } catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
