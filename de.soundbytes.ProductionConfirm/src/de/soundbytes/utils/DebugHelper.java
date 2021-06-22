/******************************************************************************
 * Copyright (C) 2021 Andreas Sumerauer                                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

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
