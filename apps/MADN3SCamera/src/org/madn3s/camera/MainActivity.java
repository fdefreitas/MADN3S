package org.madn3s.camera;

import static org.madn3s.camera.MADN3SCamera.position;
import static org.madn3s.camera.MADN3SCamera.projectName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String tag = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private Context mContext;
    private TextView configTextView;
    private RelativeLayout workingLayout;
    private ImageView takePictureImageView;
    private MainActivity mActivity;
    
    public JSONObject config, result;
    
    private BaseLoaderCallback mLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActivity = this;
		mContext = this;
		mCamera = MADN3SCamera.getCameraInstance();
		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, MADN3SCamera.DISCOVERABLE_TIME);
		startActivity(discoverableIntent);
		
		Intent williamWallaceIntent = new Intent(this, BraveheartMidgetService.class);
		startService(williamWallaceIntent);
		
		setUpBridges();
		
	    cameraPreview = (FrameLayout) findViewById(R.id.camera_frameLayout);
	    mPreview = new CameraPreview(this, mCamera);
	    cameraPreview.addView(mPreview);
	    
	    MADN3SCamera.mPreview = mPreview;
		MADN3SCamera.isPictureTaken = new AtomicBoolean(true);
		MADN3SCamera.isRunning = new AtomicBoolean(true);
		
		workingLayout = (RelativeLayout) findViewById(R.id.working_layout);
		workingLayout.setVisibility(View.GONE);
		configTextView = (TextView) findViewById(R.id.configs_text_view);
		takePictureImageView = (ImageView) findViewById(R.id.take_picture_imageView);
		takePictureImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCamera != null){
					mCamera.takePicture(null, null, mPictureCallback);
				} else {
					Toast.makeText(v.getContext(), "mCamera null", Toast.LENGTH_SHORT).show();
				}
			}
		});
    }
    
    /**
	 * Sets up OpenCV Init Callback <code>UniversalComms</code> Bridges and Camera Callbacks
	 */
    private void setUpBridges() {
    	mLoaderCallback = new BaseLoaderCallback(this) {
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
    	
    	HiddenMidgetReader.bridge = new UniversalComms() {	
			@Override
			public void callback(Object msg) {
				final String msgFinal =(String) msg;
				mActivity.getWindow().getDecorView().post(
					new Runnable() { 
						public void run() {
							configTextView.setText(msgFinal);
						} 
					});
				
//				Log.d("UniversalComms", "Callback. msg: " + (String)msg + ".-");
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveheartMidgetService.class);
				williamWallaceIntent.putExtra(HiddenMidgetReader.EXTRA_CALLBACK_MSG, (String)msg);
				startService(williamWallaceIntent);
			}
		};
		
		BraveheartMidgetService.cameraCallback = new UniversalComms() {
			@Override
			public void callback(Object msg) {
				config = (JSONObject) msg;
				Log.d(tag, "takePhoto. config == null? " + (config == null));
				Log.d(tag, "takePhoto. mPctureCallback == null? " + (mPictureCallback == null));
				Log.d(tag, "takePhoto. mContext == null? " + (mContext == null));
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
		};
	}

	@Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(! MADN3SCamera.isOpenCvLoaded) {
        	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
        }
//        MADN3SCamera.isPictureTaken.set(true);
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
//		MADN3SCamera.isRunning.set(false);
	}
    

    @Override
    protected void onPause() {
        super.onPause();
//        releaseCamera();
//        MADN3SCamera.isPictureTaken.set(false);
    }
//
//    private void releaseCamera(){
//        if (mCamera != null){
//            mCamera.release();
//            mCamera = null;
//        }
//    }
    
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
    
    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		@SuppressLint("SimpleDateFormat")
		@Override
        public void onPictureTaken(byte[] data, Camera camera) {
			final byte[] mData = data;
			final Camera mCamera = camera;
			Log.d(tag, "onPicureTaken.");
			
			new AsyncTask<Void, Void, JSONObject>() {
				
				@Override
				protected void onPreExecute() {
					Log.d(tag, "onPicureTaken. onPreExecute.");
					setWorking();
					Log.d(tag, "onPicureTaken. onPreExecute.Done");
				}
				
				@Override
				protected JSONObject doInBackground(Void... params) {
					JSONObject result = new JSONObject();
					Log.d(tag, "onPicureTaken. doInBackground.");
					try {
						Log.d(tag, "onPicureTaken. config: " + config.toString());
						position = config.getString("side");
						projectName = config.getString("project_name");
					} catch (Exception e) {
						position ="default";
						projectName ="default";
						Log.d(tag, "onPicureTaken. Error parsing JSONObject. Fallback to default config");
						e.printStackTrace();
					}
					
					Log.d(tag, "onPicureTaken. doInBackground.");
		        	MidgetOfSeville figaro = new MidgetOfSeville();
		        	int orientation;
		            BitmapFactory.Options options = new BitmapFactory.Options();
		            options.inSampleSize = 6;
		            options.inDither = false;
		            options.inPurgeable = true;
		            options.inInputShareable = true;
		            options.inTempStorage = new byte[32 * 1024];
		            options.inPreferredConfig = Bitmap.Config.RGB_565;
		            Bitmap bMap = BitmapFactory.decodeByteArray(mData, 0, mData.length, options);
		            
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
		                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) 
		                		+ "/MADN3SCamera", projectName);
		                if (!mediaStorageDir.exists()){
		                    if (!mediaStorageDir.mkdirs()){
		                        Log.d(tag, "Failed to create directory");
		                        return null;
		                    }
		                }
		                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		                String filePath = mediaStorageDir.getPath() + File.separator + position + "_" + timeStamp + ".jpg";
		                
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
		                Log.d(tag, result.toString());
		                
		                filePath = String.format(mediaStorageDir.getPath() + File.separator + position + "grabCut" + "_" + timeStamp + ".jpg");
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
					
					return result;
				}

				@Override
				protected void onPostExecute(JSONObject result) {
					mCamera.startPreview();
					Log.d(tag, "onPicureTaken. Call to onStartCommand() with result inside intent");
					if(result != null){
						Log.d(tag, "onPicureTaken. onPostExecute. result null");
						Intent williamWallaceIntent = new Intent(mContext, BraveheartMidgetService.class);
						williamWallaceIntent.putExtra("result", result.toString());
						startService(williamWallaceIntent);
					}
					unsetWorking();
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			Log.d(tag, "onPicureTaken. after Execute.");
        }
	};
	private FrameLayout cameraPreview;

	private void setWorking(){
		takePictureImageView.setClickable(false);
		takePictureImageView.setEnabled(false);
		cameraPreview.setVisibility(View.GONE);
		workingLayout.setVisibility(View.VISIBLE);
	}
	
	private void unsetWorking(){
		takePictureImageView.setClickable(true);
		takePictureImageView.setEnabled(true);
		cameraPreview.setVisibility(View.VISIBLE);
		workingLayout.setVisibility(View.GONE);
	}
}
