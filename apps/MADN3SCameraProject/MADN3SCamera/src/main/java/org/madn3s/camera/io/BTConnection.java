package org.madn3s.camera.io;

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

import java.io.IOException;
import java.util.UUID;

/**
 * Created by inaki on 2/1/14.
 */
public class BTConnection {
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
        Log.d("BTConnection", "Llamando a AsyncTask");
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
}


