package org.madn3s.camera;

import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.view.ViewGroup.MarginLayoutParams;

public class MidgetOfSeville {

	private static final String tag = "MidgetOfSeville";
	private static final Scalar ZERO_SCALAR = new Scalar(0);
	private int iterCount = 1;

	/**
	 * <a href="http://hiankun.blogspot.com/2013/08/try-grabcut-using-opencv.html">Example Code</a> 
	 * @param imgBitmap
	 * @throws JSONException 
	 */
	public JSONArray shapeUp(Bitmap imgBitmap) throws JSONException {
		String savePath;
		int height = imgBitmap.getHeight();
		int width = imgBitmap.getWidth();
		
		Mat imgMat = new Mat(height, width, CvType.CV_8UC3);
		Utils.bitmapToMat(imgBitmap, imgMat);
		Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB);
		Log.d(tag, "imgMat after cvtColor:" + imgMat.toString());
		
		
		Mat mask = new Mat(height, width, CvType.CV_8UC3, ZERO_SCALAR);
		Log.d(tag, "mask: " + mask.toString());
		
		double yOffset = width /4; 
		
		Point p1 = new Point(yOffset, 0);
		Point p2 = new Point(yOffset * 3 ,height);
		
		Rect rect = new Rect(p1, p2);
		Log.d(tag, "rect: " + rect.toString());
		
		Mat bgdModel = new Mat();
		Mat fgdModel = new Mat();
		
		Imgproc.grabCut(imgMat, mask, rect, bgdModel, fgdModel, iterCount, Imgproc.GC_INIT_WITH_RECT);
		
		Log.d(tag, "grabCut done,moving on");
	
		Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), mask, Core.CMP_EQ);
		
		Mat foreground = new Mat(imgMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
		
		imgMat.copyTo(foreground, mask);
		
		double iCannyLowerThreshold = 35;  
	    double iCannyUpperThreshold = 75;  
	    
	    Mat cannyfied = new Mat(height, width, CvType.CV_8UC3, ZERO_SCALAR);
	                  
	    Imgproc.Canny(mask, cannyfied, iCannyLowerThreshold, iCannyUpperThreshold); 
	    
	    Log.d(tag, "canny done,moving on");
	    
	    MatOfPoint MOPcorners = new MatOfPoint();
	    
		Imgproc.goodFeaturesToTrack(cannyfied, MOPcorners, 50, 0.01, 30);  
        
		Log.d(tag, "goodFeatures done,moving on");
	              
	    List<Point> corners = MOPcorners.toList();  
	    Scalar color =  new Scalar(255, 0, 0);
	              
	    Log.d(tag, "starting point printing for");
	    JSONObject actual =  new JSONObject();
	    JSONArray result = new JSONArray();
	    for (Point point : corners){
	    	actual.put("x", point.x);
	    	actual.put("y", point.y);
	    	result.put(actual);
			Core.circle(foreground, point, 10, color);
        }  
	  
	    Log.d(tag, "finished point printing");
		
	    Log.d(tag, "result " + result.toString(1));
	    
		Bitmap maskBitmap = Bitmap.createBitmap(mask.cols(), mask.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(mask, maskBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(maskBitmap, "mask");
		Log.d(tag, "mask saved to " + savePath);
		
		Bitmap fgdBitmap = Bitmap.createBitmap(foreground.cols(), foreground.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(foreground, fgdBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(fgdBitmap, "fgd");
		Log.d(tag, "foreground saved to " + savePath);
		
		Bitmap cannyBitmap = Bitmap.createBitmap(cannyfied.cols(), cannyfied.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(cannyfied, cannyBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(cannyBitmap, "canny");
		Log.d(tag, "canny saved to " + savePath);
		
		imgMat.release();
		mask.release();
		fgdModel.release();
		bgdModel.release();
		foreground.release();
		
		imgBitmap.recycle();
		maskBitmap.recycle();
		fgdBitmap.recycle();
		
		Log.d(tag, "grabCut done");
		return result;
	}
	
	public JSONArray shapeUp(String filePath) throws JSONException{
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		Bitmap imgBitmap = BitmapFactory.decodeFile(filePath, options);
		Log.d(tag, "imgBitmap config: " + imgBitmap.getConfig().toString() + " hasAlpha: " + imgBitmap.hasAlpha());		
		return shapeUp(imgBitmap);
	}
}
