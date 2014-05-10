package org.madn3s.camera;

import java.io.FileOutputStream;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

public class MidgetOfSeville {

	private static final String tag = "MidgetOfSeville";
	private static final Scalar ZERO_SCALAR = new Scalar(0);
	private int iterCount = 1;

	/**
	 * <a href="http://hiankun.blogspot.com/2013/08/try-grabcut-using-opencv.html">Example Code</a> 
	 * @param imgBitmap
	 */
	public void shapeUp(Bitmap imgBitmap) {
		String savePath;
		int height = imgBitmap.getHeight();
		int width = imgBitmap.getWidth();
		
		Mat imgMat = new Mat(height, width, CvType.CV_8UC3);
		Utils.bitmapToMat(imgBitmap, imgMat);
		Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2RGB);
		Log.d(tag, "imgMat after cvtColor:" + imgMat.toString());
		
		
		Mat mask = new Mat(height, width, CvType.CV_8UC3, ZERO_SCALAR);
		Log.d(tag, "mask: " + mask.toString());
		
		Rect rect = new Rect(0, 0, height/2, width/2);
		Log.d(tag, "rect: " + rect.toString());
		
		Mat bgdModel = new Mat();
		Mat fgdModel = new Mat();
		
		Imgproc.grabCut(imgMat, mask, rect, bgdModel, fgdModel, iterCount, Imgproc.GC_INIT_WITH_RECT);
		
//		Log.d(tag, "fgdModel: " + fgdModel.toString());
//		Log.d(tag, "bgdModel: " + bgdModel.toString());
//
//		Bitmap maskBitmap = Bitmap.createBitmap(mask.cols(), mask.rows(), Bitmap.Config.RGB_565);
//		Utils.matToBitmap(mask, maskBitmap);
//		savePath = MADN3SCamera.saveBitmapAsJpeg(maskBitmap, "mask");
//		Log.d(tag, "mask saved to " + savePath);
//		
//		Bitmap bgdBitmap = Bitmap.createBitmap(bgdModel.cols(), bgdModel.rows(), Bitmap.Config.RGB_565);
//		Utils.matToBitmap(bgdModel, bgdBitmap);
//		savePath = MADN3SCamera.saveBitmapAsJpeg(bgdBitmap, "bgdModel");
//		Log.d(tag, "fgd saved to " + savePath);
		
//		Bitmap fgdBitmap = Bitmap.createBitmap(fgdModel.cols(), fgdModel.rows(), Bitmap.Config.RGB_565);
//		Utils.matToBitmap(fgdModel, fgdBitmap);
//		savePath = MADN3SCamera.saveBitmapAsJpeg(fgdBitmap, "fgdModel");
//		Log.d(tag, "fgd saved to " + savePath);
//		Log.d(tag, "grabCut done");
	
		Core.compare(mask, new Scalar(Imgproc.GC_PR_FGD), mask, Core.CMP_EQ);
		Mat foreground = new Mat(imgMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
		imgMat.copyTo(foreground, mask);
		
		Bitmap maskBitmap = Bitmap.createBitmap(mask.cols(), mask.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(mask, maskBitmap);
		savePath = MADN3SCamera.saveBitmapAsJpeg(maskBitmap, "mask");
		Log.d(tag, "mask saved to " + savePath);
		Log.d(tag, "grabCut done");
	}
	
	public void shapeUp(String filePath){
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		Bitmap imgBitmap = BitmapFactory.decodeFile(filePath, options);
		Log.d(tag, "imgBitmap config: " + imgBitmap.getConfig().toString() + " hasAlpha: " + imgBitmap.hasAlpha());		
		shapeUp(imgBitmap);
	}
	
	public Bitmap backgroundSubtracting(String path) {
		Options options = new Options();
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		Bitmap imgBitmap = BitmapFactory.decodeFile(path, options);
		Log.d(tag, "imgBitmap config: " + imgBitmap.getConfig().toString() + " hasAlpha: " + imgBitmap.hasAlpha());
		return backgroundSubtracting(imgBitmap);
	}
	
	public Bitmap backgroundSubtracting(Bitmap sourceBitmap) {
		Mat sourceMat = new Mat();
		Utils.bitmapToMat(sourceBitmap, sourceMat);
		sourceMat.assignTo(sourceMat, CvType.CV_8UC3);
		Log.d(tag, sourceMat.toString());
		
		Mat firstMask = new Mat(sourceBitmap.getHeight(), sourceBitmap.getWidth(), CvType.CV_8UC3);
		
		Point tl = new Point();
		tl.x = 0;
		tl.y = 0;
		
		Point br = new Point();
		br.x = 1;
		br.y = 1;
		
		Rect rect = new Rect(tl, br);
		
		Mat bgModel = new Mat(sourceBitmap.getHeight(), sourceBitmap.getWidth(), CvType.CV_8UC3);
		Mat fgModel = new Mat(sourceBitmap.getHeight(), sourceBitmap.getWidth(), CvType.CV_8UC3);
		
		Imgproc.grabCut(sourceMat, firstMask, rect, bgModel, fgModel, 1);
		
		Mat background = new Mat(sourceBitmap.getHeight(), sourceBitmap.getWidth(), CvType.CV_8UC3);
		Mat mask;
		Mat source = new Mat(1, 1, CvType.CV_8UC3, new Scalar(3.0));
		Mat dst = new Mat(sourceBitmap.getHeight(), sourceBitmap.getWidth(), CvType.CV_8UC3);
		Scalar color = new Scalar(255, 0, 0, 255);
		
		Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

		Mat foreground = new Mat(sourceMat.size(), CvType.CV_8UC3, new Scalar(255,
				255, 255));
		sourceMat.copyTo(foreground, firstMask);

		Core.rectangle(sourceMat, tl, br, color);

		Mat tmp = new Mat();
		Imgproc.resize(background, tmp, sourceMat.size());
		background = tmp;
		mask = new Mat(foreground.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));

		Imgproc.cvtColor(foreground, mask, 6/* COLOR_BGR2GRAY */);
		Imgproc.threshold(mask, mask, 254, 255, 1 /* THRESH_BINARY_INV */);

		Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
		background.copyTo(dst);

		background.setTo(vals, mask);

		Core.add(background, foreground, dst, mask);

		firstMask.release();
		source.release();
		bgModel.release();
		fgModel.release();
		vals.release();
		
		Bitmap resultBitmap = null;
		Utils.matToBitmap(background, resultBitmap);
		
		return resultBitmap;
	}

}
