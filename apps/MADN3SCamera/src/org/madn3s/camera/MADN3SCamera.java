package org.madn3s.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by inaki on 3/4/14.
 */
public class MADN3SCamera extends Application {
    public static final String TAG = "MADN3SCamera";
    public static final int DISCOVERABLE_TIME = 300000;
    public static Context appContext;
	public static boolean isOpenCvLoaded = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static String projectName;
    public static String position;
    private static File appDirectory;
    
    public static CameraPreview mPreview;
    
    public static AtomicBoolean isPictureTaken; 
    public static AtomicBoolean isRunning; 
    
    private Handler mBluetoothHandler;
    private Handler.Callback mBluetoothHandlerCallback = null;
    
    private static Camera mCamera;

    @SuppressLint("HandlerLeak")
	@Override
    public void onCreate() {
        super.onCreate();
        appContext = super.getBaseContext();
        appDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        		, appContext.getString(R.string.app_name));
        
        mBluetoothHandler = new Handler() {
    	    public void handleMessage(android.os.Message msg) {
    	        if (mBluetoothHandlerCallback != null) {
    	            mBluetoothHandlerCallback.handleMessage(msg);
    	        }
    	    };
    	};
    	
    	Consts.init();
    }
    
    public static File getAppDirectory(){
    	return appDirectory;
    }

    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }
    
    public static Uri getOutputMediaFileUri(int type, String projectName, String position){
        return Uri.fromFile(getOutputMediaFile(type, projectName, position));
    }

    @SuppressLint("SimpleDateFormat")
	public static File getOutputMediaFile(int type){
    	return getOutputMediaFile(type, projectName, position);
    }

    @SuppressLint("SimpleDateFormat")
	public static File getOutputMediaFile(int type, String projectName, String iteration){
        File mediaStorageDir = new File(getAppDirectory(), projectName);

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        if(iteration == null){
        	iteration = "";
        }
        
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename;
        File mediaFile;
        
        if (type == MEDIA_TYPE_IMAGE){
            filename = "IMG_" + iteration + "_" + timeStamp + Consts.IMAGE_EXT;
        } else {
            return null;
        }
        
        mediaFile = new File(mediaStorageDir.getPath(), filename);

        return mediaFile;
    }
    
    public static String saveBitmapAsJpeg(Bitmap bitmap, String position){
    	FileOutputStream out;
        try {
            final File imgFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, projectName, position);

            out = new FileOutputStream(imgFile.getAbsoluteFile());
            bitmap.compress(Consts.BITMAP_COMPRESS_FORMAT, Consts.COMPRESSION_QUALITY, out);
            
            new Handler(Looper.getMainLooper()).post(new Runnable() {             
                @Override
                public void run() { 
                	Toast.makeText(appContext, imgFile.getName(), Toast.LENGTH_SHORT).show();
                }
              });
            
            return imgFile.getPath();
            
        } catch (FileNotFoundException e) {
            Log.e(position, "saveBitmapAsJpeg: No se pudo guardar el Bitmap", e);
            return null;
        }
    }
    
    public static Camera getCameraInstance(){
        if(mCamera == null){
	        try {
	            mCamera = Camera.open();
	            mCamera.setDisplayOrientation(90);
	            mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
	        }
	        catch (Exception e){
	            e.printStackTrace();
	            mCamera = null;
	        }
        }
        return mCamera;
    }

	public Handler getBluetoothHandler() {
		return mBluetoothHandler;
	}
	
	public void setBluetoothHandlerCallBack(Handler.Callback callback) {
	    this.mBluetoothHandlerCallback = callback;
	}
}
