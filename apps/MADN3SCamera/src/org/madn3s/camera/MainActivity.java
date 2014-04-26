package org.madn3s.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.madn3s.camera.io.BTConnection;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String tag = "MainActivity";
	private BluetoothAdapter mBluetoothAdapter;
    private static BTConnection btc;
    private static MidgetOfSeville figaro;
    private Camera mCamera;
    private CameraPreview mPreview;
    private String projectName;
    private String position;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(tag, "OpenCV loaded successfully");
                    MADN3SCamera.isOpenCvLoaded = true;
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, MADN3SCamera.DISCOVERABLE_TIME);
//        startActivity(discoverableIntent);


        mCamera = MADN3SCamera.getCameraInstance();
        figaro = new MidgetOfSeville();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_frameLayout);
        preview.addView(mPreview);

        Button button = (Button) findViewById(R.id.connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
    //                    btc = new BTConnection();
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
        projectName = "first";//obtener este valor desde la tablet
        position = "right";//obtener este valor desde la tablet
    }
    
    @Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
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

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

         /*   File pictureFile = MADN3SCamera.getOutputMediaFile(MADN3SCamera.MEDIA_TYPE_IMAGE, projectName, position);
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
           */
        	int orientation;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
            options.inDither = false; // Disable Dithering mode
            options.inPurgeable = true; // Tell to gc that whether it needs free
            // memory, the Bitmap can be cleared
            options.inInputShareable = true; // Which kind of reference will be
            // used to recover the Bitmap
            // data after being clear, when
            // it will be used in the future
            options.inTempStorage = new byte[32 * 1024];
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            // others devices
            if(bMap.getHeight() < bMap.getWidth()){
                orientation = 90;
            } else {
                orientation = 0;
            }

            Bitmap bMapRotate;
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
                        bMap.getHeight(), matrix, true);
            } else
                bMapRotate = Bitmap.createScaledBitmap(bMap, bMap.getWidth(),
                        bMap.getHeight(), true);


            FileOutputStream out;
            try {
                File mediaStorageDir = new File(Environment
                		.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                		+"/MADN3SCamera", projectName);

                if (!mediaStorageDir.exists()){
                    if (!mediaStorageDir.mkdirs()){
                        Log.d("ERROR", "failed to create directory");
                        return;
                    }
                }

                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                
                out = new FileOutputStream(String.format(mediaStorageDir.getPath() 
                		+ File.separator + position + "_" + timeStamp + ".jpg"));
                
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                
                Toast.makeText(getBaseContext(), out.toString(), Toast.LENGTH_SHORT).show();
                
//                btc.notifyPictureTaken();
//              figaro.shapeUp(out);
                Bitmap resultBitmap = figaro.backgroundSubtracting(bMapRotate);
                
                out = new FileOutputStream(String.format(mediaStorageDir.getPath() 
                		+ File.separator + position + "grabCut" + "_" + timeStamp + ".jpg"));
                
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                
                if (bMapRotate != null) {
                    bMapRotate.recycle();
                    bMapRotate = null;
                }
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            camera.startPreview();
        }
    };

}
