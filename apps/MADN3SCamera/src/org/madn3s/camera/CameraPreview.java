package org.madn3s.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        mCamera = camera;
        boolean hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);//this is lying...
        if(hasFlash){
            Log.d(MADN3SCamera.TAG, "FLASH AVAILABLE");//+mCamera.getParameters().getFlashMode());
//            Camera.Parameters p = mCamera.getParameters();
//            p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//            mCamera.setParameters(p);
        } else {
            Log.d(MADN3SCamera.TAG, "NO FLASH AVAILABLE");
        }
        //mCamera.startPreview();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(MADN3SCamera.TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        if(mCamera.getParameters().getSupportedPreviewSizes() != null){
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
           // int pos = mCamera.getParameters().getSupportedPreviewSizes().size() - 1;
            Camera.Size previewSize = getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);
//            for(Camera.Size sizeE : mCamera.getParameters().getSupportedPreviewSizes()){
//                Log.d("CameraPreview", "height: " + sizeE.height + " width: " + sizeE.width);
//            }

            Log.d("CameraPreview", "SELECTED height: " + previewSize.height + " width: " + previewSize.width);
            Log.d("CameraPreview", "Screen Size height: " + height + " width: " + width);

            mHolder.setFixedSize(previewSize.width, previewSize.height);
        }

        Log.d(MADN3SCamera.TAG, "Before : " + mCamera.getParameters().get("rotation"));

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
       //     mCamera.setDisplayOrientation(90);
            Camera.Parameters p = mCamera.getParameters();
            p.set("orientation", "portrait");
            mCamera.setParameters(p);
//            mCamera.getParameters().set("orientation", "landscape");
//            mCamera.getParameters().setRotation(270);
        }else{
            Log.d(MADN3SCamera.TAG, "LANDSCAPE "+getResources().getConfiguration().orientation);
            mCamera.getParameters().set("orientation", "landscape");
            mCamera.getParameters().setRotation(0);
        }

        Log.d(MADN3SCamera.TAG, "After: " + mCamera.getParameters().get("rotation"));

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            Log.d(MADN3SCamera.TAG, "setFlash = "+mCamera.getParameters().getFlashMode());
        } catch (Exception e) {
            Log.d(MADN3SCamera.TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        Log.d(MADN3SCamera.TAG, " minDiff " + minDiff + " targetHeight " + targetHeight + " targetRatio " + targetRatio + " ASPECT_TOLERANCE " + ASPECT_TOLERANCE);
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            Log.d(MADN3SCamera.TAG, "size.width " + size.width + " size.height " + size.height + " ratio " + ratio + " Math.abs(ratio - targetRatio) " + Math.abs(ratio - targetRatio));
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE){
                continue;
            }
            Log.d(MADN3SCamera.TAG, "Math.abs(size.height - targetHeight) " + Math.abs(size.height - targetHeight));
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        Log.d(MADN3SCamera.TAG, "optimalSize " + (optimalSize==null?"no":"yes"));
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Log.d(MADN3SCamera.TAG, "optimalSize " + (optimalSize==null?"no":"yes"));
        return optimalSize;
    }
}