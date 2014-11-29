package org.madn3s.camera;

import static org.madn3s.camera.MADN3SCamera.position;
import static org.madn3s.camera.MADN3SCamera.projectName;
import static org.madn3s.camera.Consts.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String tag = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout cameraPreview;
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
		
		setDiscoverableBt();
		setUpBridges();
		
		Intent williamWallaceIntent = new Intent(this, BraveheartMidgetService.class);
		startService(williamWallaceIntent);
		
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
					Toast.makeText(v.getContext(), "onClick. mCamera null", Toast.LENGTH_SHORT).show();
				}
			}
		});
				
		
		cameraPreview = (FrameLayout) findViewById(R.id.camera_frameLayout);
		MADN3SCamera.isPictureTaken = new AtomicBoolean(true);
		MADN3SCamera.isRunning = new AtomicBoolean(true);
    }

	@Override
    public void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(! MADN3SCamera.isOpenCvLoaded) {
        	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
        }
        
        startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        releaseCamera();
    }
    
    /**
     * Sets up {@link Camera} instance and the {@link CameraPreview} associated with it
     */
    protected void startCamera() {
		mCamera = MADN3SCamera.getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        cameraPreview.removeAllViews();
        cameraPreview.addView(mPreview);
        mCamera.startPreview();
	}
    
    /**
     * Releases {@link Camera} instance
     */
    protected void releaseCamera(){
        if (mCamera != null){
        	mCamera.stopPreview();
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
    
    /**
     * Relaunch activity with request to set Device discoverable over Bluetooth
     */
	private void setDiscoverableBt() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, MADN3SCamera.DISCOVERABLE_TIME);
		startActivity(discoverableIntent);
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
    	
        //Received Message Callback
    	HiddenMidgetReader.bridge = new UniversalComms() {	
			@Override
			public void callback(Object msg) {
				final String msgFinal = (String) msg;
				mActivity.getWindow().getDecorView().post(
					new Runnable() { 
						public void run() {
							configTextView.setText(msgFinal);
						} 
					});
				Intent williamWallaceIntent = new Intent(getBaseContext(), BraveheartMidgetService.class);
				williamWallaceIntent.putExtra(Consts.EXTRA_CALLBACK_MSG, (String) msg);
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
		    			//TODO revisar por que int y no bool
						result.put(Consts.KEY_ERROR, 1);
						Log.d(tag, "takePhoto. result: " + result.toString(1));
					} catch (JSONException e) {
						e.printStackTrace();
					}
		    	}
			}
		};
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
					setWorking();
				}
				
				@Override
				protected JSONObject doInBackground(Void... params) {
					JSONObject result = new JSONObject();
					Log.d(tag, "onPicureTaken. doInBackground.");
					try {
						Log.d(tag, "onPicureTaken. config: " + config.toString());
						position = config.getString(Consts.KEY_SIDE);
						projectName = config.getString(Consts.KEY_PROJECT_NAME);
					} catch (Exception e) {
						position = Consts.VALUE_DEFAULT_POSITION;
						projectName = Consts.VALUE_DEFAULT_PROJECT_NAME;
						Log.e(tag, "onPicureTaken. Error parsing JSONObject. Fallback to default config", e);
					}
					
					Log.d(tag, "onPicureTaken. doInBackground.");
		        	MidgetOfSeville figaro = new MidgetOfSeville();
		        	int orientation;
		            Bitmap bMap = BitmapFactory.decodeByteArray(mData, 0, mData.length
		            		, Consts.bitmapFactoryOptions);
		            
		            if(bMap.getHeight() < bMap.getWidth()){
		                orientation = 90;
		            } else {
		            	orientation = 0;
		            }
		            
		            Bitmap bMapRotate;
		            if (orientation != 0) {
		                Matrix matrix = new Matrix();
		                matrix.postRotate(orientation);
		                bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight()
		                		, matrix, true);
		            } else {
		                bMapRotate = Bitmap.createScaledBitmap(bMap, bMap.getWidth(), bMap.getHeight(), true);
		            }
		            
		            try {
		            	String filePath = MADN3SCamera.saveBitmapAsJpeg(bMapRotate, position);
		            	
		            	Log.d(tag, "filePath desde MainActivity: " + filePath);
		                
		                JSONObject resultJsonObject = figaro.shapeUp(filePath, config);
		                
		                JSONArray pointsJson = resultJsonObject.getJSONArray(KEY_POINTS);
		                
		                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name)
		                		, MODE_PRIVATE);
		        		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		        		sharedPreferencesEditor.putString(KEY_FILE_PATH, resultJsonObject.getString(KEY_FILE_PATH));
		        		sharedPreferencesEditor.commit();
		                
		                if(pointsJson != null && pointsJson.length() > 0){
		                	result.put(KEY_MD5, resultJsonObject.get(KEY_MD5));
		                	result.put(Consts.KEY_ERROR, false);
		                	result.put(Consts.KEY_POINTS, pointsJson);
		                } else {
		                	Log.d(tag, "pointsJson: " + pointsJson.toString(1));
		                	result.put(Consts.KEY_ERROR, true);
		                }
		                Log.d(tag, "mPictureCalback. result: " + result.toString(1));
		                
		            } catch (JSONException e) {
						e.printStackTrace();
					}
					
					return result;
				}

				@Override
				protected void onPostExecute(JSONObject result) {
					mCamera.startPreview();
					if(result != null){
						Intent williamWallaceIntent = new Intent(mContext, BraveheartMidgetService.class);
						williamWallaceIntent.putExtra(Consts.EXTRA_RESULT, result.toString());
						startService(williamWallaceIntent);
					}
					unsetWorking();
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
	};

	/**
	 * Updates layout to reflect the application is working on processing the picture
	 */
	private void setWorking(){
		takePictureImageView.setClickable(false);
		takePictureImageView.setEnabled(false);
		cameraPreview.setVisibility(View.GONE);
		workingLayout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Updates layout to reflect the application is done working on processing the picture
	 */
	private void unsetWorking(){
		takePictureImageView.setClickable(true);
		takePictureImageView.setEnabled(true);
		cameraPreview.setVisibility(View.VISIBLE);
		workingLayout.setVisibility(View.GONE);
	}
}
