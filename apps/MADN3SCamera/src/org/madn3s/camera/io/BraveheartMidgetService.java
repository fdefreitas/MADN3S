package org.madn3s.camera.io;



import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import org.madn3s.camera.MADN3SCamera;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
        Log.d(tag, "Onstart Command");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            mBluetoothServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BTConnection.SERVICE_NAME, BTConnection.APP_UUID);
            mBluetoothServerSocketWeakReference = new WeakReference<BluetoothServerSocket>(mBluetoothServerSocket);
            HiddenMidgetConnector task = new HiddenMidgetConnector(mBluetoothServerSocketWeakReference);
            Log.d(tag, "Ejecutando a HiddenMidgetConnector");
            task.execute();
        } catch (IOException e) {
        	//TODO transmitir error inicializando servicio
        	Log.d(tag, "No se pudo inicializar mBluetoothServerSocket. Imprimiendo Stack Trace:");
            e.printStackTrace();
        }
        
        String stopservice = intent.getStringExtra("stopservice");
        if (stopservice != null && stopservice.length() > 0) {
            stopSelf();
        }
        bridge.callback(intent);
        return START_NOT_STICKY;
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e(tag, "VIVE!!!!!!!");	
	}
    
}
