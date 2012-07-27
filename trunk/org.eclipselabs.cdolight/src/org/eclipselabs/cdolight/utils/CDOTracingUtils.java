package org.eclipselabs.cdolight.utils;

public class CDOTracingUtils {
	
	private static StringBuilder strBuilder = new StringBuilder();

	private CDOTracingUtils(){
	}

	public static synchronized void appendTrace(String string) {
		try {
			strBuilder.append(string);
			strBuilder.append("\n");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static synchronized String dumpTrace() {
		String dump = strBuilder.toString();
		strBuilder.setLength(0);
		return dump;
	}

}
