package org.eclipselabs.cdolight.utils;

public class CDOTracingUtils {
	
	private static final String HTML_TOOLTIP_STYLES = 
			"<style type='text/css'>" +
			"	.tooltip div{display:none;}" +
			"	.tooltip:hover div{display:inline; background-color: #FFFFCC; position:absolute; border-color:blue; border-style: solid; border-width: 2px; min-width:200px; min-height:200px;}" +
			"</style>";
	
	private static StringBuilder strBuilder = new StringBuilder();

	private CDOTracingUtils(){
	}

	public static synchronized void append(String string) {
		try {
			strBuilder.append(string);
			strBuilder.append("\n");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static synchronized void appendHtmlTrace(String string) {
		try {
			strBuilder.append(string);
			strBuilder.append(" <a href='#hint' class='tooltip'>(stack trace)<div class='info'>");
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (int i = 2; i < stackTrace.length && i < 22; i++) {
				strBuilder.append(stackTrace[i].toString() + "<br/>");
			}
			strBuilder.append("</div></a><br/>");
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
	
	public static synchronized String dumpHtmlTrace() {
		String dump = HTML_TOOLTIP_STYLES + "\n\n" + strBuilder.toString();
		strBuilder.setLength(0);
		return dump;
	}
	
	
}
