package org.eclipselabs.cdolight.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CDOLightUtils {
	
	private static BufferedWriter bWriter;

	private CDOLightUtils(){
		try{
		bWriter = new BufferedWriter(new FileWriter(new File(CDOLightConstants.TRACE_FILENAME)));
		}catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void appendTrace(String string) {
		try {
			bWriter.append(string);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void closeIO() {
		try {
			bWriter.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	

}
