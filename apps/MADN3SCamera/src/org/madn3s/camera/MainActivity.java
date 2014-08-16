package org.madn3s.camera;

import static org.madn3s.camera.MADN3SCamera.position;
import static org.madn3s.camera.MADN3SCamera.projectName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.madn3s.camera.io.BTConnection;
import org.madn3s.camera.io.BraveheartMidgetService;
import org.madn3s.camera.io.HiddenMidgetReader;
import org.madn3s.camera.io.UniversalComms;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
		
		//Reiniciar Activity colocando el Dispositivo en "Discoverable"
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, MADN3SCamera.DISCOVERABLE_TIME);
		startActivity(discoverableIntent);
		
		HiddenMidgetReader.bridge = new UniversalComms() {
			
			@Override
			public void callback(Object msg) {
				Log.d(tag + ".UniversalComms", "Callback. msg: " + (String)msg + ".-");
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveheartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		Intent williamWallaceIntent = new Intent(this, BraveheartMidgetService.class);
		startService(williamWallaceIntent);
		
		
		
        
//        mCamera = MADN3SCamera.getCameraInstance();
//        figaro = new MidgetOfSeville();
//
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_frameLayout);
//        preview.addView(mPreview);
//
//        Button button = (Button) findViewById(R.id.connect_button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////            	btc = new BTConnection();
//            	if(mCamera != null){
//            		mCamera.takePicture(null, null, mPictureCallback);
//            	} else {
//            		Toast.makeText(v.getContext(), "mCamera == null", Toast.LENGTH_SHORT).show();
//            	}
//            }
//        });
        projectName = "first";//obtener este valor desde la tablet
        position = "right";//obtener este valor desde la tablet
    }
    
    @Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(! MADN3SCamera.isOpenCvLoaded) {
        	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
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

    //TODO comparar con Callback de Midgeteer
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
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
                
                String filePath = mediaStorageDir.getPath() 
                		+ File.separator + position + "_" + timeStamp + ".jpg"; 
                
                out = new FileOutputStream(filePath);
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                
                Toast.makeText(getBaseContext(), "Imagen almacenada en " + filePath, Toast.LENGTH_SHORT).show();
                
//                btc.notifyPictureTaken();
//              figaro.shapeUp(out);
                JSONArray result = figaro.shapeUp(filePath);
                
                
          //      btc.notifyPictureTaken(result);
                //MADN3SCamera.saveBitmapAsJpeg(figaro.backgroundSubtracting(filePath), "backgroundSubstract");
                
                out = new FileOutputStream(String.format(mediaStorageDir.getPath() 
                		+ File.separator + position + "grabCut" + "_" + timeStamp + ".jpg"));
                
                bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
                
                if (bMapRotate != null) {
                    bMapRotate.recycle();
                    bMapRotate = null;
                }
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            camera.startPreview();
        }
        
        
        
    };

}
