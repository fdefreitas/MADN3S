package org.madn3s.camera.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.camera.MADN3SCamera;
import org.madn3s.camera.Midgeteer;
import org.madn3s.camera.R;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BraveheartMidgetService extends IntentService {
	public BraveheartMidgetService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public BraveheartMidgetService() {
		super("BraveheartMidgetService");
		// TODO Auto-generated constructor stub
	}

	public static final String BT_DEVICE = "btdevice";
	private static final String tag = "BTConnection";
	
	public static final String SERVICE_NAME ="MADN3S";
	public static final UUID APP_UUID = UUID.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
	
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
	private static final String TOAST = null;
	
	//@TODO incluir dentro de Handler Custom
	private static final int MESSAGE_TOAST = 0;
	private static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_WRITE = 2;
    
    private static BluetoothConnectionThread mBluetoothConnectionThread;
    private static BluetoothConnectedThread mBluetoothConnectedThread;

    private static Handler mHandler = null;
    private BluetoothAdapter mBluetoothAdapter;
    public static int mState = STATE_NONE;
    
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;
	
//	@Override
//    public void onCreate() {
//        Log.d("PrinterService", "Service started");
//        super.onCreate();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        mHandler = ((MADN3SCamera) getApplication()).getBluetoothHandler();
//        Log.d(tag, "mHandler "+ mHandler == null ? "NULL" : mHandler.toString());
//        Log.d(tag, "mBinder "+ mBinder == null ? "NULL" : mBinder.toString());
//        return mBinder;
//    }
//
//    public class LocalBinder extends Binder {
//        BraveheartMidgetService getService() {
//            return BraveheartMidgetService.this;
//        }
//    }
//
//
//    private final IBinder mBinder = new LocalBinder();
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("PrinterService", "Onstart Command");
////        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////        if (mBluetoothAdapter != null) {
////            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
////            deviceName = device.getName();
////            String macAddress = device.getAddress();
////            if (macAddress != null && macAddress.length() > 0) {
////                connectToDevice(macAddress);
////            } else {
////                stopSelf();
////                return 0;
////            }
////        }
////        String stopservice = intent.getStringExtra("stopservice");
////        if (stopservice != null && stopservice.length() > 0) {
////            stop();
////        }
//        return START_STICKY;
//    }

    private synchronized void connectToDevice(String macAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING) {
            if (mBluetoothConnectionThread != null) {
            	mBluetoothConnectionThread.cancel();
            	mBluetoothConnectionThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mBluetoothConnectedThread != null) {
        	mBluetoothConnectedThread.cancel();
        	mBluetoothConnectedThread = null;
        }
        mBluetoothConnectionThread = new BluetoothConnectionThread(device);
        mBluetoothConnectionThread.start();
        setState(STATE_CONNECTING);
    }

    private void setState(int state) {
        BraveheartMidgetService.mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

//    public synchronized void stop() {
//        setState(STATE_NONE);
//        if (mBluetoothConnectionThread != null) {
//        	mBluetoothConnectionThread.cancel();
//        	mBluetoothConnectionThread = null;
//        }
//
//        if (mBluetoothConnectedThread != null) {
//        	mBluetoothConnectedThread.cancel();
//        	mBluetoothConnectedThread = null;
//        }
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.cancelDiscovery();
//        }
//        stopSelf();
//    }
//
//    @Override
//    public boolean stopService(Intent name) {
//        setState(STATE_NONE);
//        if (mBluetoothConnectedThread != null) {
//        	mBluetoothConnectionThread.cancel();
//        	mBluetoothConnectionThread = null;
//        }
//
//        if (mBluetoothConnectedThread != null) {
//        	mBluetoothConnectedThread.cancel();
//        	mBluetoothConnectedThread = null;
//        }
//        mBluetoothAdapter.cancelDiscovery();
//        return super.stopService(name);
//    }

    private static Object obj = new Object();

    public static void write(byte[] out) {
        // Create temporary object
        BluetoothConnectionThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (obj) {
            if (mState != STATE_CONNECTED)
                return;
            r = mBluetoothConnectionThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        // Cancel the thread that completed the connection
        if (mBluetoothConnectionThread != null) {
        	mBluetoothConnectionThread.cancel();
        	mBluetoothConnectionThread = null;
        }

        // Cancel any thread currently running a connection
        if (mBluetoothConnectedThread != null) {
        	mBluetoothConnectedThread.cancel();
        	mBluetoothConnectedThread = null;
        }

        mBluetoothConnectionThread = new BluetoothConnectionThread(mmSocket);
        mBluetoothConnectionThread.start();

        // Message msg =
        // mHandler.obtainMessage(AbstractActivity.MESSAGE_DEVICE_NAME);
        // Bundle bundle = new Bundle();
        // bundle.putString(AbstractActivity.DEVICE_NAME, "p25");
        // msg.setData(bundle);
        // mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }
    
    public boolean encodeData(InputStream inputStream){
    	BufferedReader buffer;
    	String line;
    	StringBuilder stringBuilder = new StringBuilder();
    	
    	try {
    		InputStreamReader in = new InputStreamReader(inputStream);
            buffer = new BufferedReader(in);
            
            while ((line = buffer.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONArray points = new JSONArray();
            JSONObject jsonMsg = readMessage(stringBuilder.toString());
//            Midgeteer midgeteer = new Midgeteer(jsonMsg, getBaseContext(), points, MADN3SCamera.getCameraInstance());
//            midgeteer.start();
            //enviar points
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    	return false;
    }
    
    public JSONObject readMessage(String str){
        JSONObject jsonMsg = null;
        try {
			jsonMsg = new JSONObject(str);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return jsonMsg;
    }
    

    private class BluetoothConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public BluetoothConnectedThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;

            }
            synchronized (BraveheartMidgetService.this) {
                mBluetoothConnectionThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("BraveheartMidgetService", "close() of connect socket failed", e);
            }
        }
    }
    
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Printer Service", "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (!encodeData(mmInStream)) {
                        mState = STATE_NONE;
                        connectionLost();
                        break;
                    } else {
                    }
                    // mHandler.obtainMessage(AbstractActivity.MESSAGE_READ,
                    // bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
//                    BraveheartMidgetService.this.stop();
                    break;
                }

            }
        }

        private byte[] btBuff;


        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("PrinterService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
                Log.e("PrinterService", "close() of connect socket failed", e);
            }
        }

    }

	public void trace(String msg) {
        Log.d("AbstractActivity", msg);
        toast(msg);
    }

    public void toast(String msg) {
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void onDestroy() {
//        stop();
//        Log.d("Printer Service", "Destroyed");
//        super.onDestroy();
//    }

    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                case 3:

                    break;

                case 4:

                    break;
                case 5:
                    break;

                case -1:
                    break;
                }
            }
            super.handleMessage(msg);
        }

    };
    
    private void connectionFailed() {
//        BraveheartMidgetService.this.stop();
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
//        bundle.putString(TOAST, getString(R.string.error_connect_failed));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
//        BraveheartMidgetService.this.stop();
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
//        bundle.putString(TOAST, getString(R.string.error_connect_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public class BluetoothConnectionThread extends Thread {
        private static final int STATE_NONE = 0;
    	private final BluetoothSocket mmSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
    	private int mState;

        public BluetoothConnectionThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Printer Service", "temp sockets not created", e);
            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

    	public BluetoothConnectionThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
                tmpIn = tmp.getInputStream();
                tmpOut = tmp.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
		}

		@Override
        public void run() {
            while (true) {
                try {
                    if (!encodeData(mInputStream)) {
                        mState = STATE_NONE;
                        connectionLost();
                        break;
                    } else {
                    }
                    // mHandler.obtainMessage(AbstractActivity.MESSAGE_READ,
                    // bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
//                    BraveheartMidgetService.this.stop();
                    break;
                }

            }
        }

        private boolean encodeData(InputStream mInputStream2) {
    		// TODO Auto-generated method stub
    		return false;
    	}

    	private byte[] btBuff;
    	private Object mHandler;


        public void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(AbstractActivity.MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("PrinterService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
                Log.e("PrinterService", "close() of connect socket failed", e);
            }
        }
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.e(tag, "VIVE!!!!!!!");
	}
    
    
}
