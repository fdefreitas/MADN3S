package org.madn3s.camera.io;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.camera.MADN3SCamera;
import org.madn3s.camera.R;

import java.io.File;

import android.os.Environment;
import android.util.Log;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


public class BraveheartMidgetService extends IntentService {
	
	
	public static String projectName;
	public static String side;
	private JSONObject result;
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
	public static WeakReference<BluetoothSocket> mSocketWeakReference;
    private static Handler mHandler = null;
    private BluetoothAdapter mBluetoothAdapter;
    public static int mState = STATE_NONE;
    
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;
	public static UniversalComms cameraCallback;
    
    private JSONObject config;
    
    
	
    @Override
	public void onDestroy() {
		super.onDestroy();
//		releaseCamera();
	}

	public BraveheartMidgetService() {
		super(tag);
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
    	if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG) || intent.hasExtra("result")){
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
	            
	        } catch (IOException e) {
	        	Log.d(tag, "No se pudo inicializar mBluetoothServerSocket.");
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
		try {
			String jsonString = "{}";
			JSONObject msg;
			if(intent.hasExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG)){
				jsonString = intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_MSG);
				msg = new JSONObject(jsonString);
				if(msg.has("action")){
					String action = msg.getString("action");
					side = msg.getString("side");
					if(msg.has("project_name")){
						projectName = msg.getString("project_name");
					}
					if(config == null){//kind of cheating...
						config = msg;
					}
					Log.d(tag, "action: " + action);
					if(action.equalsIgnoreCase("config")){
						config = msg;
						//TODO guardar en sharedPrefs
						MADN3SCamera.isPictureTaken.set(true);
					} else if(action.equalsIgnoreCase("take_picture")){
						cameraCallback.callback(config);
					} else if(action.equalsIgnoreCase("send_picture")) {
						sendPicture();
					} else if(action.equalsIgnoreCase("end_project")){
						if(msg.has("clean") && msg.getBoolean("clean")){
							cleanTakenPictures(projectName);
						}
						projectName = null;
					} else if(action.equalsIgnoreCase("calibrate")){
						calibrate();
						sendResult();
					} else if(action.equalsIgnoreCase("exit_app")){
						Log.d(tag, "onHandleIntent: NO LLEGA");	
					} else {
						Log.d(tag, "onHandleIntent: QUE MIERDA ES ESTO? " + action);	
					}
				}
			} else if (intent.hasExtra("result")) {
				jsonString = intent.getExtras().getString("result");
				result = new JSONObject(jsonString);
				//TODO ordenar y arreglar
				if(result.has("error")){
					Log.d(tag, "recibido Error.");
					sendResult();
					MADN3SCamera.isPictureTaken.set(true);
				}
			}
		} catch (JSONException e) {
			Log.d(tag, "Could Not Parse JSON");
			e.printStackTrace();
		}
	}

	private void sendPicture() {
		Log.d(tag, "mSocketWeakReference == null: " + (mSocketWeakReference == null));
		//TODO buscar imagen guardada
		SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
		
		String filepath = sharedPreferences.getString("filepath", null);
		//TODO over options a constante junto con las del callback de camara
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		
		Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
		
		if(filepath != null){
			if(mSocketWeakReference != null){
				HiddenMidgetWriter writerTask = new HiddenMidgetWriter(mSocketWeakReference, bitmap);
		        Log.d(tag, "Ejecutando a HiddenMidgetWriter desde sendPicture");
		        writerTask.execute();
			}
		} else {
			Log.d(tag, "filepath : null");
		}
	}

	private void calibrate() throws JSONException {
		try {
			result.put("error", false);
	    	if(result.has("points")){
	    		result.remove("points");
	    	} 
		} catch (Exception e) {
			e.printStackTrace();
			result.put("error", true);
		}
	}

	private void sendResult() {
		Log.d(tag, "mSocketWeakReference == null: " + (mSocketWeakReference == null));
		if(mSocketWeakReference != null){
			HiddenMidgetWriter writerTask = new HiddenMidgetWriter(mSocketWeakReference, result.toString());
	        Log.d(tag, "Ejecutando a HiddenMidgetWriter desde sendResult");
	        writerTask.execute();
		}
	}

	private void cleanTakenPictures(String projectName) {
		Log.d(tag, "Limpiando " + projectName);
		File projectMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +"/MADN3SCamera", projectName);
		if (projectMediaStorageDir.exists()){
			String[] files = projectMediaStorageDir.list();
			if(files != null){
				for (int i = 0; i < files.length; i++) {
					Log.d(tag, "Limpiando " + files[i]);
		            new File(projectMediaStorageDir, files[i]).delete();
		        }
			}
			projectMediaStorageDir.delete();
        }
	}
	
}
