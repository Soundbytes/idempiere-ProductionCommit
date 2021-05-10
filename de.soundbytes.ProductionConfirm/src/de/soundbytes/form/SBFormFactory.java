package de.soundbytes.form;

import java.lang.ClassLoader;
import java.util.logging.Level;

import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.CLogger;


public class SBFormFactory implements IFormFactory {

	protected transient CLogger log = CLogger.getCLogger(getClass());

	@Override
	public ADForm newFormInstance(String formName) {
		if (formName.startsWith("de.soundbytes.form")) {
			Object form = null;
			Class<?> clazz = null;
			ClassLoader loader = getClass().getClassLoader(); 

			try {
				clazz = loader.loadClass(formName);
			} catch (Exception e) {
				log.log(Level.WARNING, "Load Form Class failed in " +formName, e);
			}
			if (clazz != null) {
				try {
					form = new WProductionConfirm();
				} catch (Exception e) {
					log.log(Level.WARNING, "Form Class Initiate failed in " + formName, e);
				}
			}
			if (form != null) {
				if (form instanceof ADForm) {
					return (ADForm) form;					
				} else if (form instanceof IFormController) {
					IFormController controller = (IFormController) form;
					ADForm adForm = controller.getForm();
					adForm.setICustomForm(controller);
					return adForm;
				}
			}
		}
		return null;
	}
}
