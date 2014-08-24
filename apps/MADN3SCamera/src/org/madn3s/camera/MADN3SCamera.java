package org.madn3s.camera;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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
    
    public static CameraPreview mPreview;
    
    public static AtomicBoolean isPictureTaken; 
    public static AtomicBoolean isRunning; 
    
    private Handler mBluetoothHandler;
    private Handler.Callback mBluetoothHandlerCallback = null;
    
    private static Camera mCamera;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = super.getBaseContext();
        
        mBluetoothHandler = new Handler() {
    	    public void handleMessage(android.os.Message msg) {
    	        if (mBluetoothHandlerCallback != null) {
    	            mBluetoothHandlerCallback.handleMessage(msg);
    	        }
    	    };
    	};
    }

    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appContext.getString(R.string.app_name));

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Uri getOutputMediaFileUri(int type, String projectName, String position){
        return Uri.fromFile(getOutputMediaFile(type, projectName, position));
    }

    public static File getOutputMediaFile(int type, String projectName, String position){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+appContext.getString(R.string.app_name), projectName);

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + position + "_" + timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + position + "_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    public static String saveBitmapAsJpeg(Bitmap bitmap, String tag){
    	FileOutputStream out;
        try {
            File imgFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, projectName, tag);

            out = new FileOutputStream(imgFile.getAbsoluteFile());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            
            Toast.makeText(appContext, imgFile.getName(), Toast.LENGTH_SHORT).show();
            
            return imgFile.getName();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(tag, "saveBitmapAsJpeg: No se pudo guardar el Bitmap");
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
