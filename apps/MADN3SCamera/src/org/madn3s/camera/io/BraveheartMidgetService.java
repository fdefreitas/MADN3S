package org.madn3s.camera.io;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.camera.MADN3SCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.madn3s.camera.MidgetOfSeville;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


public class BraveheartMidgetService extends IntentService {
	
	
	public static String projectName;
	public static String side;
	private JSONArray result;
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
    
    private JSONObject config;
    private Camera mCamera;
	
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
	            
	            mCamera = (Camera)intent.getExtras().get("camera");
	            
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
			String jsonString = intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_MSG);
			JSONObject msg = new JSONObject(jsonString);
			side = msg.getString("action");
			projectName = msg.getString("project_name");
			if(msg.has("action")){
				String action = msg.getString("action");
				if(action.equalsIgnoreCase("config")){
					config = msg;
				} else if(action.equalsIgnoreCase("photo")){
					takePhoto();
					if(result != null){
						sendResult();
					}
				} else if(action.equalsIgnoreCase("end_project")){
					if(msg.has("clean") && msg.getBoolean("clean")){
						cleanTakenPictures(projectName);
					}
				} else if(action.equalsIgnoreCase("exit_app")){
					Log.d(tag, "onHandleIntent: NO LLEGA");	
				} else {
					Log.d(tag, "onHandleIntent: QUE MIERDA ES ESTO? " + action);	
				}
			}
			printString = new JSONObject(intent.getExtras().getString(HiddenMidgetReader.EXTRA_CALLBACK_MSG)).toString(1);
		} catch (JSONException e) {
			printString = "Could Not Parse JSON";
			e.printStackTrace();
		}
		Log.d(tag, "onHandleIntent:");	
		Log.d(tag, printString);
	}

	private void sendResult() {
		// TODO Auto-generated method stub
		
	}

	private void cleanTakenPictures(String projectName) {
		// TODO Auto-generated method stub
		
	}

	private void takePhoto() {
		// TODO Auto-generated method stub
		if(mCamera != null){
    		mCamera.takePicture(null, null, mPictureCallback);
    	} else {
    		result = null;
    	}
		
	}
	
	private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		@Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	MidgetOfSeville figaro = new MidgetOfSeville();
        	int orientation;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
            options.inDither = false; // Disable Dithering mode
            options.inPurgeable = true; // Tell to gc that whether it needs free
            options.inInputShareable = true; // Which kind of reference will be
            options.inTempStorage = new byte[32 * 1024];
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if(bMap.getHeight() < bMap.getWidth()){
                orientation = 90;
            } else {
                orientation = 0;
            }
            Bitmap bMapRotate;
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
            } else {
                bMapRotate = Bitmap.createScaledBitmap(bMap, bMap.getWidth(), bMap.getHeight(), true);
            }
            FileOutputStream out;
            try {
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +"/MADN3SCamera", projectName);
                if (!mediaStorageDir.exists()){
                    if (!mediaStorageDir.mkdirs()){
                        Log.d("ERROR", "failed to create directory");
                        return;
                    }
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filePath = mediaStorageDir.getPath() + File.separator + side + "_" + timeStamp + ".jpg"; 
                out = new FileOutputStream(filePath);
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                result = figaro.shapeUp(filePath, config);
                out = new FileOutputStream(String.format(mediaStorageDir.getPath() + File.separator + side + "grabCut" + "_" + timeStamp + ".jpg"));
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                if (bMapRotate != null) {
                    bMapRotate.recycle();
                    bMapRotate = null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
				e.printStackTrace();
			}
	        camera.startPreview();
        }
	};
    
}
