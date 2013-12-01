package org.madn3s.robot.common;

import org.madn3s.sandbox.Movement;

import lejos.nxt.Button;




public class Main {
	public static void main(String[] args) {
		Utils.printToScreen("MADN3S V1");
		Movement m = new Movement();
		int button = Button.waitForAnyPress();
		while(!Utils.buttonIsEscape(button)){
			if(Utils.buttonIsRight(button)){
				m.test1();
			} else if(Utils.buttonIsLeft(button)){
				m.test4();
			} else if(Utils.buttonIsEnter(button)){
				m.test2();
			}
			button = Button.waitForAnyPress();
		}
	}
}
