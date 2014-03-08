package org.madn3s.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        Log.d(MADN3SCamera.TAG, "flash = "+mCamera.getParameters().getFlashMode());
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(p);
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
            int pos = mCamera.getParameters().getSupportedPreviewSizes().size() - 1;
            Camera.Size previewSize = mCamera.getParameters().getSupportedPreviewSizes().get(2);
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
}