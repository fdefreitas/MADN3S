package org.madn3s.camera;

import android.bluetooth.BluetoothAdapter;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import org.madn3s.camera.io.BTConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends ActionBarActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private static BTConnection btc;
    private Camera mCamera;
    private CameraPreview mPreview;
    private String projectName;
    private String position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, MADN3SCamera.DISCOVERABLE_TIME);
//        startActivity(discoverableIntent);


        mCamera = MADN3SCamera.getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_frameLayout);
        preview.addView(mPreview);

        Button button = (Button) findViewById(R.id.connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
    //                    btc = new BTConnection();
                mCamera.takePicture(null, null, mPicture);
            }
        });
        projectName = "first";//obtener este valor desde la tablet
        position = "right";//obtener este valor desde la tablet

    }
    @Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = MADN3SCamera.getOutputMediaFile(MADN3SCamera.MEDIA_TYPE_IMAGE, projectName, position);
            if (pictureFile == null){
                Log.d(MADN3SCamera.TAG, "Error creating media file, check storage permissions ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(MADN3SCamera.TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(MADN3SCamera.TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

}
