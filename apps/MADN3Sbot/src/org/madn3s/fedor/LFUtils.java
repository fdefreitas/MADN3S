package org.madn3s.fedor;

import org.madn3s.sandbox.HTSensors;

public class LFUtils {

	private static final int NUMBER_OF_SAMPLES = 20;

	public LFUtils() {
	}

	public static int getAvgColorValue() {

		int sum = 0;
		for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
			sum += HTSensors.getInstance().getCentralColor();
		}
		return sum / NUMBER_OF_SAMPLES;
	}

}