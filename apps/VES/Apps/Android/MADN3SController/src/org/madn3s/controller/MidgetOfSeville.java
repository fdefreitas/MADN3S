package org.madn3s.controller;

import static org.madn3s.controller.Consts.*;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

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
				Bitmap rightGrayBitmap = Bitmap.createBitmap(rightMat.cols(), rightMat.rows(), Bitmap.Config.RGB_565);
				Utils.matToBitmap(rightMat, rightGrayBitmap);
				MADN3SController.saveBitmapAsJpeg(rightGrayBitmap, "rightGray");
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
				Bitmap leftGrayBitmap = Bitmap.createBitmap(leftMat.cols(), leftMat.rows(), Bitmap.Config.RGB_565);
				Utils.matToBitmap(leftMat, leftGrayBitmap);
				MADN3SController.saveBitmapAsJpeg(leftGrayBitmap, "letfGray");
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
			
			MatOfByte status = new MatOfByte();
			MatOfFloat err = new MatOfFloat();
			
			Video.calcOpticalFlowPyrLK(leftMat, rightMat, leftMop, rightMop, status, err);
			
			byte[] statusBytes = status.toArray();
			Log.d(tag, "lenghts. leftPoints: " + leftPoints.size()
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
}
