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
	private WeakReference<BluetoothSocket> mSocketWeakReference;
    private static Handler mHandler = null;
    private BluetoothAdapter mBluetoothAdapter;
    public static int mState = STATE_NONE;
    
    public static String deviceName;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static BluetoothDevice device = null;
	public static UniversalComms cameraCallback;
    
    private JSONObject config;
    private Camera mCamera;
    
    
    
	
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
	            
	            mCamera = MADN3SCamera.mPreview.getmCamera();
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
					projectName = msg.getString("project_name");
					if(config == null){//kind of cheating...
						config = msg;
					}
					if(action.equalsIgnoreCase("config")){
						config = msg;
					} else if(action.equalsIgnoreCase("photo")){
						Log.d(tag, "action: photo");
						Log.d(tag, "call to cameraCallback()");
//						takePhoto();
						Log.d(tag, "config == null? " + (config == null));
						cameraCallback.callback(config);
					} else if(action.equalsIgnoreCase("end_project")){
						if(msg.has("clean") && msg.getBoolean("clean")){
							cleanTakenPictures(projectName);
						}
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
				msg = new JSONObject(jsonString);
				
				if(msg.has("error")){
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
		Log.d(tag, "inside sendResult()");
	}

	private void cleanTakenPictures(String projectName) {
		// TODO Auto-generated method stub
		
	}

	private void takePhoto() {
		if(mCamera != null){
			Log.d(tag, "takePhoto. mCamera != null. calling TakePicture()");
    		mCamera.takePicture(null, null, mPictureCallback);
    	} else {
    		Log.d(tag, "takePhoto. mCamera == null.");
    		result = new JSONObject();
    		try {
				result.put("error", 1);
				Log.d(tag, "takePhoto. result: " + result.toString(1));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
	}
	
	private void releaseCamera(){
        if (mCamera != null){
        	mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
	
	private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		@Override
        public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(tag, "onPicureTaken. Callback triggered.");
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
                        Log.d(tag, "Failed to create directory");
                        return;
                    }
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filePath = mediaStorageDir.getPath() + File.separator + side + "_" + timeStamp + ".jpg"; 
                out = new FileOutputStream(filePath);
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                Log.d(tag, "Saving as JPEG file: " + filePath);
                
                JSONArray resultSP = figaro.shapeUp(filePath, config);
                
                if(resultSP != null && resultSP.length() > 0){
                	result.put("error", false);
                	result.put("points", resultSP);
                } else {
                	result.put("error", true);
                }
                Log.d(tag, "mPictureCalback. result: ");
                Log.d(tag, resultSP.toString(1));
                
                filePath = String.format(mediaStorageDir.getPath() + File.separator + side + "grabCut" + "_" + timeStamp + ".jpg");
                out = new FileOutputStream(filePath);
                Log.d(tag, "Saving as JPEG grabCut file: " + filePath);
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
			Log.d(tag, "onPicureTaken. Call to sendResult()");
			sendResult();
        }
	};
}
