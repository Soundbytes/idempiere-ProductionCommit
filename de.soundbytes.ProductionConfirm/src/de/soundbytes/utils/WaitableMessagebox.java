package de.soundbytes.utils;

import java.util.concurrent.CountDownLatch;
import org.adempiere.util.Callback;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Messagebox;

public class WaitableMessagebox {
	public CountDownLatch latch;
	int m_result = 111;
	
	int showMe(String message, String title, int buttons, String icon) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m_result;
	}
	
	public static int showDialog(String message, String title, int buttons, String icon) {
		return new WaitableMessagebox().showMe(message, title, buttons, icon);
	}
}


