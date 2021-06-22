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

import java.util.concurrent.CountDownLatch;
import org.adempiere.util.Callback;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Messagebox;

public class WaitableMessagebox {
	public CountDownLatch latch;
	private int m_result = 111;
	
	private int showMe(String message, String title, int buttons, String icon) {
		latch = new CountDownLatch(1);
		AEnv.executeAsyncDesktopTask(new Runnable() {
		@Override
			public void run() {
				Messagebox.showDialog(
						message, 
						title, 
						buttons, 
						icon,
						new Callback<Integer>() {
							@Override
							public void onCallback(Integer resVal) {
								m_result = resVal.intValue();
								latch.countDown();
							}
							
						}, 
						false);
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m_result;
	}
	
	public static int show(String message, String title, int buttons, String icon) {
		return new WaitableMessagebox().showMe(message, title, buttons, icon);
	}
}


