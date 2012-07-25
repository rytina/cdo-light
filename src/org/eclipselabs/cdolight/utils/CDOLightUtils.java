package org.eclipselabs.cdolight.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class CDOLightUtils {
	
	private static BufferedWriter bWriter;
	static{
		try{
			bWriter = new BufferedWriter(new FileWriter(new File(CDOLightConstants.TRACE_FILEPATH)));
		}catch (Throwable e) {
		e.printStackTrace();
		}
	}
	
	

	private CDOLightUtils(){
	}

	public static void appendTrace(String string) {
		try {
			bWriter.append(string);
			bWriter.append("\n");
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
