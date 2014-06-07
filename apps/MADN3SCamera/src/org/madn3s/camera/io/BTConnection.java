package org.madn3s.camera.io;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.util.UUID;

import org.json.JSONArray;

/**
 * Created by inaki on 2/1/14.
 */
public class BTConnection {
    private static final String tag = "BTConnection";
	public static final String SERVICE_NAME ="MADN3S";
    public static final UUID APP_UUID = UUID.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");

    private static BTConnection instance;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket controllerSocket;

    public BTConnection(){
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case MESSAGE_TOAST:
//                        Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
//                        break;
//                    case MESSAGE_STATE_CHANGE:
//                        mState = msg.arg1;
//                        switch(mState){
//                            case NXTTalker.STATE_CONNECTED:
//                                nxtConnectingProgressBar.setVisibility(View.GONE);
//                                nxtConnectedImageView.setVisibility(View.VISIBLE);
//                                break;
//                            case NXTTalker.STATE_NONE:
//                                nxtConnectingProgressBar.setVisibility(View.GONE);
//                                nxtNotConnectedImageView.setVisibility(View.VISIBLE);
//                                nxtConnectedImageView.setVisibility(View.GONE);
//                                break;
//                            case NXTTalker.STATE_CONNECTING:
//                                nxtConnectingProgressBar.setVisibility(View.VISIBLE);
//                                nxtNotConnectedImageView.setVisibility(View.GONE);
//                                nxtConnectedImageView.setVisibility(View.GONE);
//                                break;
//                        }
//                        break;
//                }
            }
        };

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();

        HiddenMidgetAttackAsyncTask task = new HiddenMidgetAttackAsyncTask(mBluetoothAdapter);
        Log.d(tag, "Llamando a AsyncTask");
        task.execute();
    }

    public static BTConnection getInstance(){
        if(instance == null) instance = new BTConnection();
        return instance;
    }

    public BluetoothSocket getControllerSocket() {
        return controllerSocket;
    }

    public void setControllerSocket(BluetoothSocket controllerSocket) {
        this.controllerSocket = controllerSocket;
    }

	public void notifyPictureTaken(JSONArray toSend) {
		try {
			writeMessage(toSend.toString());
		} catch (InterruptedException e) {
			Log.d(tag, "I pitty the fool!");
			e.printStackTrace();
		}
		
	}
	
	public void writeMessage(String msg) throws InterruptedException{
        if(controllerSocket!=null){
            try {
                OutputStreamWriter out = new OutputStreamWriter(controllerSocket.getOutputStream());
                out.write(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readMessage(){
        BufferedReader buffer;
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        
        if(controllerSocket!=null){
            try {
                InputStreamReader in = new InputStreamReader(controllerSocket.getInputStream());
                buffer = new BufferedReader(in);
                
                while ((line = buffer.readLine()) != null) {
                    stringBuilder.append(line);
                }
                
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	
	
}


