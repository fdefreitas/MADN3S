package org.madn3s.robot.common;

import lejos.nxt.Button;
import lejos.nxt.LCD;

public class Utils {
	public static final int SCREEN_WIDTH = 0x10;
	public static final int SCREEN_HEIGTH = 0x7;
	
	public static void printToScreen(String text, int x, int y, boolean clean){
		if(clean){
			LCD.clear();
		}
		if(text.length() > SCREEN_WIDTH){
			int total = (int)Math.ceil((double)text.length() / (double)SCREEN_WIDTH);
			int index = 0;
			for(int i = 0; i < total; ++i){
				LCD.drawString(text.substring(index, i!=total-1?index+SCREEN_WIDTH:text.length()), x, y+i);
				index += SCREEN_WIDTH;
			}
		} else {
			LCD.drawString(text, x, y);
		}
		LCD.refresh(); 
	}
	
	public static void printToScreen(String text, int x, int y){
		printToScreen(text, x, y, true);
	}
	
	public static void printToScreen(String text){
		printToScreen(text, 0, 0, true);
	}
	
	public static boolean buttonIsRight(int buttonCode){
		return buttonCode == Button.ID_RIGHT;
	}
	
	public static boolean buttonIsLeft(int buttonCode){
		return buttonCode == Button.ID_LEFT;
	}
	
	public static boolean buttonIsEscape(int buttonCode){
		return buttonCode == Button.ID_ESCAPE;
	}
	
	public static boolean buttonIsEnter(int buttonCode){
		return buttonCode == Button.ID_ENTER;
	}

}
