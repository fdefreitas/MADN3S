package org.madn3s.camera.io;



import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.camera.MADN3SCamera;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BraveheartMidgetService extends IntentService {
	
	

	public static final String BT_DEVICE = "btdevice";
	private static final String tag = "BraveheartMidgetService";
	
	public static final String SERVICE_NAME ="MADN3S";
	public static final UUID APP_UUID = UUID.fromString("65da7fe0-8b80-11e3-baa8-0800200c9a66");
	
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
	private static final String TOAST = null;
	
	//TODO incluir dentro de Handler Custom
	private static final int MESSAGE_TOAST = 0;
	private static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_WRITE = 2;
    
	private BluetoothServerSocket mBluetoothServerSocket;
	private WeakReference<BluetoothServerSocket> mBluetoothServerSocketWeakReference;
	private BluetoothSocket mSocket;
	private WeakReference<BluetoothSocket> mSocketWeakReference;
    private static Handler mHandler = null;
    private BluetoothAdapter mBluetoothAdapter;
    public static int mState = STATE_NONE;
    
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;
	
    public BraveheartMidgetService() {
		super("BraveheartMidgetService");
	}

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = ((MADN3SCamera) getApplication()).getBluetoothHandler();
        Log.d(tag, "mHandler "+ mHandler == null ? "NULL" : mHandler.toString());
        Log.d(tag, "mBinder "+ mBinder == null ? "NULL" : mBinder.toString());
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BraveheartMidgetService getService() {
            return BraveheartMidgetService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG)){
    		Log.d(tag, "Onstart Command. Llamando a onHandleIntent.");
    		return super.onStartCommand(intent,flags,startId);
    	} else {
	        Log.d(tag, "Onstart Command");
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        try {
	            mBluetoothServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BTConnection.SERVICE_NAME, BTConnection.APP_UUID);
	            mBluetoothServerSocketWeakReference = new WeakReference<BluetoothServerSocket>(mBluetoothServerSocket);
	            
	            mSocket = null;
	            mSocketWeakReference = null;
	            
	            HiddenMidgetConnector connectorTask = new HiddenMidgetConnector(mBluetoothServerSocketWeakReference, mSocketWeakReference);
	            Log.d(tag, "Ejecutando a HiddenMidgetConnector");
	            connectorTask.execute();
	            
	            //@ Moviendo a Connector
//	            HiddenMidgetReader readerHandlerThreadThread = new HiddenMidgetReader("readerTask", mSocketWeakReference);
//	            Log.d(tag, "Ejecutando a HiddenMidgetReader");
//	            readerHandlerThreadThread.start();
	        } catch (IOException e) {
	        	//TODO transmitir error inicializando servicio
	        	Log.d(tag, "No se pudo inicializar mBluetoothServerSocket. Imprimiendo Stack Trace:");
	            e.printStackTrace();
	        }
	        
	        String stopservice = intent.getStringExtra("stopservice");
	        if (stopservice != null && stopservice.length() > 0) {
	            stopSelf();
	        }
	        return START_NOT_STICKY;
    	}
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		String printString;
		try {
			printString = new JSONObject(intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_MSG)).toString(1);
		} catch (JSONException e) {
			printString = "Could Not Parse JSON";
			e.printStackTrace();
		}
		Log.d(tag, "onHandleIntent:");	
		Log.d(tag, printString);
	}
    
}
