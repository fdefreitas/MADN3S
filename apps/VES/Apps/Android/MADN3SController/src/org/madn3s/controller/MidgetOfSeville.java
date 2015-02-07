package org.madn3s.controller;

import static org.madn3s.controller.Consts.*;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class MidgetOfSeville {
	private static final String tag = MidgetOfSeville.class.getSimpleName();

	public static void calculateFrameOpticalFlow(JSONObject data) throws JSONException{
		Log.d(tag, "calculateFrameOpticalFlow. data: " + data);
		JSONObject rightJson;
		JSONObject leftJson;
		JSONArray rightJsonArray;
		JSONArray leftJsonArray;
		String leftFilepath = null;
		String rightFilepath = null;
		Bitmap tempBitmap = null;
		Mat leftMat = null;
		Mat rightMat = null;
		ArrayList<Point> leftPoints = new ArrayList<>();
		ArrayList<Point> rightPoints = new ArrayList<>();
		MatOfPoint2f leftMop;
		MatOfPoint2f rightMop;
		
		try{
			rightJson = data.getJSONObject(SIDE_RIGHT);
			rightFilepath = rightJson.getString(KEY_FILE_PATH);
			rightJsonArray = rightJson.getJSONArray(KEY_POINTS);
			leftJson = data.getJSONObject(SIDE_LEFT);
			leftFilepath = leftJson.getString(KEY_FILE_PATH);
			leftJsonArray = leftJson.getJSONArray(KEY_POINTS);
			
			Log.d(tag, "calculateFrameOpticalFlow. Loading images.");
			if(rightFilepath != null){
				Log.d(tag, "calculateFrameOpticalFlow. Loading images. Right: " + rightFilepath);
				tempBitmap = loadBitmap(rightFilepath);
				int height = tempBitmap.getHeight();
				int width = tempBitmap.getWidth();
				
				rightMat = new Mat(height, width, CvType.CV_8UC3);
				Utils.bitmapToMat(tempBitmap, rightMat);
				Imgproc.cvtColor(rightMat, rightMat, Imgproc.COLOR_BGR2GRAY);
				
				Log.d(tag, "calculateFrameOpticalFlow. rightMat: " + (rightMat == null));
//				Bitmap rightGrayBitmap = Bitmap.createBitmap(rightMat.cols(), rightMat.rows(), Bitmap.Config.RGB_565);
//				Utils.matToBitmap(rightMat, rightGrayBitmap);
//				MADN3SController.saveBitmapAsPng(rightGrayBitmap, "rightGray");
			}
			
			if(leftFilepath != null){
				Log.d(tag, "calculateFrameOpticalFlow. Loading images. Left: " + leftFilepath);
				tempBitmap = loadBitmap(leftFilepath);
				int height = tempBitmap.getHeight();
				int width = tempBitmap.getWidth();
				
				leftMat = new Mat(height, width, CvType.CV_8UC3);
				Utils.bitmapToMat(tempBitmap, leftMat);
				Imgproc.cvtColor(leftMat, leftMat, Imgproc.COLOR_BGR2GRAY);
				
				Log.d(tag, "calculateFrameOpticalFlow. leftMat: " + (leftMat == null));
//				Bitmap leftGrayBitmap = Bitmap.createBitmap(leftMat.cols(), leftMat.rows(), Bitmap.Config.RGB_565);
//				Utils.matToBitmap(leftMat, leftGrayBitmap);
//				MADN3SController.saveBitmapAsPng(leftGrayBitmap, "letfGray");
			}
	
			JSONObject pointJson;
			for(int i = 0; i < rightJsonArray.length(); i++){
				pointJson = rightJsonArray.getJSONObject(i);
				rightPoints.add(new Point(pointJson.getDouble("x"), pointJson.getDouble("y")));
			}
			
			for(int i = 0; i < leftJsonArray.length(); i++){
				pointJson = leftJsonArray.getJSONObject(i);
				leftPoints.add(new Point(pointJson.getDouble("x"), pointJson.getDouble("y")));
			}
			
			leftMop = new MatOfPoint2f();
			leftMop.fromList(leftPoints);
			
			rightMop = new MatOfPoint2f();
			
			MatOfByte opticalFlowFoundFeatures = new MatOfByte();
			MatOfFloat err = new MatOfFloat();
			
			Video.calcOpticalFlowPyrLK(leftMat, rightMat, leftMop, rightMop, opticalFlowFoundFeatures, err);
			
//			Core.SVDecomp(src, w, u, vt);
			
			byte[] statusBytes = opticalFlowFoundFeatures.toArray();
			Log.d(tag, "lengths. leftPoints: " + leftPoints.size()
					+ " rightPoints: " + rightPoints.size()
					+ " status: " + statusBytes.length);
			
			for(int i = 0; i < leftPoints.size(); ++i){
				Log.d(tag, "calculateFrameOpticalFlow. status(" + String.format("%02d", i) + "): " + statusBytes[i]);
			}

		} catch (JSONException e){
			Log.e(tag, "calculateFrameOpticalFlow. Couldn't parse data JSONObject", e);
		} finally {
			tempBitmap.recycle();
		}
	}

	private static Bitmap loadBitmap(String filePath){
		Log.d(tag, "loadBitmap. filePath desde MidgetOfSeville: " + filePath);
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		Bitmap imgBitmap = BitmapFactory.decodeFile(filePath, options);
		Log.d(tag, "imgBitmap config: " + imgBitmap.getConfig().toString() + " hasAlpha: " + imgBitmap.hasAlpha());
		return imgBitmap;
	}
	
	public static void doStereoCalibration(){
		try{
			Log.d(tag, "doStereoCalibration. Starting");
			JSONObject calibrationJson = MADN3SController.sharedPrefsGetJSONObject(KEY_CALIBRATION);
			
			JSONObject leftSide = calibrationJson.getJSONObject(SIDE_LEFT);
			JSONObject rightSide = calibrationJson.getJSONObject(SIDE_RIGHT);
			
			Log.d(tag, "doStereoCalibration. Parsing Dist Coeffs");
			Mat rightDistCoeff = MADN3SController.getMatFromString(rightSide.getString(KEY_CALIB_DISTORTION_COEFFICIENTS));
			Mat leftDistCoeff = MADN3SController.getMatFromString(leftSide.getString(KEY_CALIB_DISTORTION_COEFFICIENTS));
			
			Log.d(tag, "doStereoCalibration. Parsing Camera Matrix");
			Mat rightCameraMatrix = MADN3SController.getMatFromString(rightSide.getString(KEY_CALIB_CAMERA_MATRIX));
			Mat leftCameraMatrix = MADN3SController.getMatFromString(leftSide.getString(KEY_CALIB_CAMERA_MATRIX));
			
			Log.d(tag, "doStereoCalibration. Parsing Image Points");
			JSONArray rightJsonImagePoints = new JSONArray(rightSide.getString(KEY_CALIB_IMAGE_POINTS));
			ArrayList<Mat> rightImagePoints = new ArrayList<Mat>(rightJsonImagePoints.length());
			for(int i = 0; i < rightJsonImagePoints.length(); ++i){
				rightImagePoints.add(MADN3SController.getImagePointFromString(rightJsonImagePoints.getString(i)));
				Log.d(tag, " rightImagePoints(" + i + ")[" + rightImagePoints.get(i).rows() + "][" + rightImagePoints.get(i).cols() + "]"
						+ ":" + rightImagePoints.get(i).dump()
						+ "  original: " + rightJsonImagePoints.getString(i));
			}
			
			JSONArray leftJsonImagePoints = new JSONArray(leftSide.getString(KEY_CALIB_IMAGE_POINTS));
			ArrayList<Mat> leftImagePoints = new ArrayList<Mat>(leftJsonImagePoints.length());
			for(int i = 0; i < leftJsonImagePoints.length(); ++i){
				leftImagePoints.add(MADN3SController.getImagePointFromString(leftJsonImagePoints.getString(i)));
//				Log.d(tag, " leftImagePoints(" + i + ")[" + leftImagePoints.get(i).rows() + "][" + leftImagePoints.get(i).cols() + "]"
//						+ ":" + leftImagePoints.get(i).dump()
//						+ "  original: " + leftJsonImagePoints.getString(i));
			}		
	
			Log.d(tag, "doStereoCalibration. Generating Object Points");
			int corners = leftImagePoints.size() >= rightImagePoints.size()? leftImagePoints.size(): rightImagePoints.size();  
			ArrayList<Mat> objectPoints = new ArrayList<Mat>();
	        objectPoints.add(Mat.zeros(corners, 1, CvType.CV_32FC3));
	        calcBoardCornerPositions(objectPoints.get(0));
	        for (int i = 1; i < corners; i++) {
	            objectPoints.add(objectPoints.get(0));
	            Log.d(tag, " objectPoints(" + i + ")[" + objectPoints.get(i).rows() + "][" + objectPoints.get(i).cols() + "]");
	        }
	        
	        Size mPatternSize = new Size(4, 11);
	        Mat R = new Mat();
	        Mat T = new Mat();
	        Mat E = new Mat();
	        Mat F = new Mat();
	        double[] termCriteriaFlags = new double[3];
	        termCriteriaFlags[0] = TermCriteria.EPS+TermCriteria.MAX_ITER;
	        termCriteriaFlags[1] = 100;
	        termCriteriaFlags[2] = 1e-5;
	        
	        Log.d(tag, "doStereoCalibration. Calibrating. objectPoints: " + objectPoints.size()
	        		+ " leftImagePoints: " + leftImagePoints.size()
	        		+ " rightImagePoints: " + rightImagePoints.size()
	        		+ " leftCameraMatrix[" + leftCameraMatrix.rows() + "][" + leftCameraMatrix.cols() + "]"
	        		+ " rightCameraMatrix[" + rightCameraMatrix.rows() + "][" + rightCameraMatrix.cols() + "]"
	        		+ " rightDistCoeff[" + rightDistCoeff.rows() + "][" + rightDistCoeff.cols() + "]"
	        		+ " leftDistCoeff[" + leftDistCoeff.rows() + "][" + leftDistCoeff.cols() + "]"
	        		);
	        Calib3d.stereoCalibrate(objectPoints, leftImagePoints, rightImagePoints
	        		, leftCameraMatrix, leftDistCoeff, rightCameraMatrix, rightDistCoeff
	        		, mPatternSize, R, T, E, F, new TermCriteria(termCriteriaFlags)
	        		, Calib3d.CALIB_FIX_ASPECT_RATIO + Calib3d.CALIB_ZERO_TANGENT_DIST + Calib3d.CALIB_SAME_FOCAL_LENGTH);
	        
	        String rStr = R.dump();
	        String tStr = T.dump();
	        String eStr = E.dump();
	        String fStr = F.dump();
	        
	        Log.d(tag, "R: " + rStr);
	        Log.d(tag, "T: " + tStr);
	        Log.d(tag, "E: " + eStr);
	        Log.d(tag, "F: " + fStr);
	        
	        MADN3SController.sharedPrefsPutString(KEY_R, rStr);
	        MADN3SController.sharedPrefsPutString(KEY_T, tStr);
	        MADN3SController.sharedPrefsPutString(KEY_E, eStr);
	        MADN3SController.sharedPrefsPutString(KEY_F, fStr);
	        
	        Mat R1 = new Mat();
	        Mat R2 = new Mat();
	        Mat P1 = new Mat();
	        Mat P2 = new Mat();
	        Mat Q = new Mat();
	        
	        Calib3d.stereoRectify(leftCameraMatrix, leftDistCoeff
	        		, rightCameraMatrix, rightDistCoeff
	        		, mPatternSize, R, T, R1, R2, P1, P2, Q);
	        
	        Mat wSingularValue = new Mat();
	        Mat uLeftOrthogonal = new Mat();
	        Mat vRightOrtogonal = new Mat();
	        Core.SVDecomp(E, wSingularValue, uLeftOrthogonal, vRightOrtogonal);
	        
	        Log.d(tag, "wSingularValue: " + wSingularValue.dump());
	        Log.d(tag, "uLeftOrthogonal: " + uLeftOrthogonal.dump());
	        Log.d(tag, "vRightOrtogonal: " + vRightOrtogonal.dump());
	        
	        //Esto probablemente hace el ojo de pescado en la camara, puede que no sea necesario a esta altura
//	        Imgproc.initUndistortRectifyMap(cameraMatrix, distCoeffs, vRightOrtogonal, newCameraMatrix, size, m1type, map1, map2);
		
		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	private static void calcBoardCornerPositions(Mat corners) {
		Size mPatternSize = new Size(4, 11);
	    int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
	    double mSquareSize = 0.0181;
        final int cn = 3;
        float positions[] = new float[mCornersSize * cn];

        for (int i = 0; i < mPatternSize.height; i++) {
            for (int j = 0; j < mPatternSize.width * cn; j += cn) {
                positions[(int) (i * mPatternSize.width * cn + j + 0)] = (2 * (j / cn) + i % 2) 
                		* (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 1)] = i * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 2)] = 0;
            }
        }
        corners.create(mCornersSize, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
    }
}
