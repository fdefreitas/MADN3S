package org.madn3s.camera;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by inaki on 3/4/14.
 */
public class MADN3SCamera extends Application {
    public static final String TAG = "MADN3SCamera";
    public static final int DISCOVERABLE_TIME = 300000;
    public static Context appContext;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = super.getBaseContext();
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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + position +"_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
            c.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//            if(c != null) Log.d(MADN3SCamera.TAG, "getCameraInstance: "+c.getParameters().flatten());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }

    @Deprecated
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager() != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
