package org.json.util;

public class StringUtil {
	public static boolean endsWith(String str, String trailingStr) {
		final int firstPos = str.length()-trailingStr.length();
		return str.lastIndexOf(trailingStr, ((firstPos>=0)?firstPos:0))>-1;
	}
}
