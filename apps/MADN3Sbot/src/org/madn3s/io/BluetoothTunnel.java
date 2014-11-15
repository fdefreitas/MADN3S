package org.madn3s.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import org.madn3s.robot.common.Utils;

import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;


public class BluetoothTunnel {
	private static BluetoothTunnel me;
	private NXTConnection connection;
	private final int CONNECTION_TIMEOUT = 60000;
	
	public BluetoothTunnel() {
		connection = Bluetooth.waitForConnection(CONNECTION_TIMEOUT, 0);
		connection.setIOMode(NXTConnection.RAW);
	}
	
	public static BluetoothTunnel getInstance(){
		if(me == null){
			me = new BluetoothTunnel();
		}
		return me;
	}
	
	public String readMessage(){
		if(connection != null){
			int byteTemp = 0;
        	int threshold = 0;
        	ByteArrayOutputStream bao = new ByteArrayOutputStream();
        	InputStream inputStream = null;
			try {
				bao.reset();
				inputStream = connection.openDataInputStream();
	        	while(true){
	        		while (inputStream.available() == 0 && threshold < 3000) { 
	                    Thread.sleep(1);
	                    threshold++;
	                }
	        		if(threshold < 3000){
	        			threshold = 0;
	        			byteTemp = inputStream.read();
	        			bao.write(byteTemp);
	            		if(byteTemp == 255){
	            			break;
	            		}
	            		Thread.sleep(1);
	        		} else {
	        			break;
	        		}
	        	}
	        	return bao != null ? bao.toString() : null;
			} catch (Exception e) {
				Utils.printToScreen(e.getMessage());
			} finally {
				try {inputStream.close();} catch (Exception e) {}
			}
		}
		return null;
	}
	
	public boolean writeMessage(String msg){
		if(connection != null){
			DataOutputStream dos = null;
			try {
				dos = connection.openDataOutputStream();
				dos.writeBytes(msg);
				return true;
			} catch (Exception e) {
				Utils.printToScreen(e.getMessage());
			} finally {
				try {dos.close();} catch (Exception e) {}
			}
		}
		return false;
	}
	
	
}
