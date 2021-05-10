package de.soundbytes.utils;


import java.util.Enumeration;
import java.util.Properties;

import org.adempiere.util.ServerContext;

public class DebugHelper {
	public static final boolean DEBUG = true;
	
	public static void dumpCtx() {
		dumpCtx(ServerContext.getCurrentInstance());
	}
	
	public static void dumpCtx(Properties ctx) {  
		System.out.println("-- listing properties --");
		Enumeration<Object> keys = ctx.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			String val = ctx.get(key).toString(); 
			if (val.length() > 40) {
				val = val.substring(0, 37) + "...";
			}
			System.out.println(key + " = " + val);
		}
	}
}
