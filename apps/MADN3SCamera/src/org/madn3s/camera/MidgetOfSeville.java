package org.madn3s.camera;

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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class MidgetOfSeville {

	private static final String tag = "MidgetOfSeville";
	private static final Scalar ZERO_SCALAR = new Scalar(0);
	private int iterCount = 1;

	/**
	 * <a href="http://hiankun.blogspot.com/2013/08/try-grabcut-using-opencv.html">Example Code</a> 
	 * @param imgBitmap
	 * @throws JSONException 
	 */
	public JSONArray shapeUp(Bitmap imgBitmap, JSONObject configs) throws JSONException {
		String savePath;
		int height = imgBitmap.getHeight();
		int width = imgBitmap.getWidth();
		
		Mat imgMat = new Mat(height, width, CvType.CV_8UC3);
		Utils.bitmapToMat(imgBitmap, imgMat);
		Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB);
		Log.d(tag, "imgMat after cvtColor:" + imgMat.toString());
		
		Mat mask = new Mat(height, width, CvType.CV_8UC3, ZERO_SCALAR);
		Log.d(tag, "mask: " + mask.toString());
		
	    double x1 = width / 4;  
	    double y1 = 0;  
	    double x2 =  x1 * 3;
	    double y2 = height;  
	    double iCannyLowerThreshold = 35;  
	    double iCannyUpperThreshold = 75;  
	    int edgeDetectionsAlgorithm = 0;
	    int maxCorners = 50;
	    double qualityLevel = 0.01;
	    double minDistance = 30;
	    
	    int ddepth = 0;
        int dx = 0;
        int dy = 0;
        
        double r = 255, g = 0, b = 0;
        int radius = 10;
		
		if (configs != null) {
			//grabCut
			if (configs.has("rectangle")) {
				JSONObject points = configs.getJSONObject("rectangle");
				JSONObject point1 = points.getJSONObject("point1");
				JSONObject point2 = points.getJSONObject("point2");
				x1 = point1.getDouble("x");
				y1 = point1.getDouble("y");
				x2 = point2.getDouble("x");
				y2 = point2.getDouble("y");
			}
			if (configs.has("iterations")) {
				iterCount = configs.getInt("iterations");
			}
			
			//goodFeaturesToTrack
			if (configs.has("maxCorners")) {
				maxCorners = configs.getInt("maxCorners");
			}
			if (configs.has("qualityLevel")) {
				qualityLevel = configs.getDouble("qualityLevel");
			}
			if (configs.has("minDistance")) {
				minDistance = configs.getInt("minDistance");
			}
			
			//edge detection
			if (configs.has("edgeDetectionsAlgorithm")) {
				edgeDetectionsAlgorithm = configs.getInt("edgeDetectionsAlgorithm");
				if (edgeDetectionsAlgorithm == 0) {//Canny
					if (configs.has("cannyLowerThreshold")) {
						iCannyLowerThreshold = configs.getDouble("cannyLowerThreshold");
					}
					if (configs.has("cannyUpperThreshold")) {
						iCannyUpperThreshold = configs.getDouble("cannyUpperThreshold");
					}
				} else if (edgeDetectionsAlgorithm == 1) {//Sobel
					if (configs.has("ddepth")) {
						ddepth = configs.getInt("ddepth");
					}
					if (configs.has("dx")) {
						dx = configs.getInt("dx");
					}
					if (configs.has("dy")) {
						dy = configs.getInt("dy");
					}
				} else {
					//Mandamos un algoritmo q no sabemos
				}
			}
			
			//extras
			if (configs.has("extras")) {
	        	if (configs.has("r")) {
	        		r = configs.getDouble("r");
	        		r = (r<0?0:(r>255?255:r));
	        	}
	        	if (configs.has("g")) {
	        		g = configs.getDouble("g");
	        		g = (g<0?0:(g>255?255:g));
	        	}
	        	if (configs.has("b")) {
	        		b = configs.getDouble("b");
	        		b = (b<0?0:(b>255?255:b));
	        	}
	        	if (configs.has("radius")) {
	        		radius = configs.getInt("radius");
	        	}
			}
		}
		
		Point p1 = new Point(x1, y1);
		Point p2 = new Point(x2, y2);
		
		Rect rect = new Rect(p1, p2);
		Log.d(tag, "rect: " + rect.toString());
		
		Mat bgdModel = new Mat();
		Mat fgdModel = new Mat();
		
		Imgproc.grabCut(imgMat, mask, rect, bgdModel, fgdModel, iterCount, Imgproc.GC_INIT_WITH_RECT);
		
		Log.d(tag, "grabCut done,moving on");
	
		Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), mask, Core.CMP_EQ);
		
		Mat foreground = new Mat(imgMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
		
		imgMat.copyTo(foreground, mask);
		
	    String edgeAlgString = "";
	    Mat edgified = new Mat(height, width, CvType.CV_8UC3, ZERO_SCALAR);
	    
	    switch (edgeDetectionsAlgorithm) {
	        case 0:
	        	edgeAlgString = "Canny";
	        	Imgproc.Canny(mask, edgified, iCannyLowerThreshold, iCannyUpperThreshold);
	            break;
	        case 1:  
	        	edgeAlgString = "Sobel";
	        	Imgproc.Sobel(mask, edgified, ddepth, dx, dy);
	            break;
//	        case 2:  
//	        	Imgproc.Canny(mask, edgified, iCannyLowerThreshold, iCannyUpperThreshold);
//	            edgeAlgString = "Canny";
//	            break;
//	        case 3:  
//	            edgeAlgString = "Canny";
//	        	Imgproc.Canny(mask, edgified, iCannyLowerThreshold, iCannyUpperThreshold);
//	            break;
	        default: 
	        	edgeAlgString = "Canny";
	        	Imgproc.Canny(mask, edgified, iCannyLowerThreshold, iCannyUpperThreshold);
	            break;
	    }
	                  
//	    Imgproc.Canny(mask, edgified, iCannyLowerThreshold, iCannyUpperThreshold); 
	    
	    Log.d(tag, edgeAlgString + " done,moving on");
	    
	    MatOfPoint MOPcorners = new MatOfPoint();
	    
		Imgproc.goodFeaturesToTrack(edgified, MOPcorners, maxCorners, qualityLevel, minDistance);  
        
		Log.d(tag, "goodFeatures done,moving on");
	              
	    List<Point> corners = MOPcorners.toList();  
	    Scalar color =  new Scalar(r, g, b);
	              
	    Log.d(tag, "starting point printing for");
	    JSONObject actual;
	    JSONArray result = new JSONArray();
	    for (Point point : corners){
	    	actual = new JSONObject();
	    	actual.put("x", point.x);
	    	actual.put("y", point.y);
	    	result.put(actual);
			Core.circle(foreground, point, radius, color);
        }  
	  
	    Log.d(tag, "finished point printing, point count: " + result.length());
		
	    Log.d(tag, "result " + result.toString(1));
	    
		Bitmap maskBitmap = Bitmap.createBitmap(mask.cols(), mask.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(mask, maskBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(maskBitmap, "mask");
		Log.d(tag, "mask saved to " + savePath);
		
		Bitmap fgdBitmap = Bitmap.createBitmap(foreground.cols(), foreground.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(foreground, fgdBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(fgdBitmap, "fgd");
		Log.d(tag, "foreground saved to " + savePath);
		
		Bitmap edgeBitmap = Bitmap.createBitmap(edgified.cols(), edgified.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(edgified, edgeBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(edgeBitmap, edgeAlgString);
		Log.d(tag, edgeAlgString + " saved to " + savePath);
		
		imgMat.release();
		mask.release();
		fgdModel.release();
		bgdModel.release();
		foreground.release();
		edgified.release();
		MOPcorners.release();
		
		imgBitmap.recycle();
		maskBitmap.recycle();
		fgdBitmap.recycle();
		edgeBitmap.recycle();
		
		Log.d(tag, "grabCut done");
		return result;
	}
	
	public JSONArray shapeUp(String filePath) throws JSONException{
		Bitmap imgBitmap = loadBitmap(filePath);		
		return shapeUp(imgBitmap, null);
	}
	
	public JSONArray shapeUp(String filePath, JSONObject configs) throws JSONException{
		Bitmap imgBitmap = loadBitmap(filePath);
		return shapeUp(imgBitmap, configs);
	}
	
	private Bitmap loadBitmap(String filePath){
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		Bitmap imgBitmap = BitmapFactory.decodeFile(filePath, options);
		Log.d(tag, "imgBitmap config: " + imgBitmap.getConfig().toString() + " hasAlpha: " + imgBitmap.hasAlpha());
		return imgBitmap;
	}
}
