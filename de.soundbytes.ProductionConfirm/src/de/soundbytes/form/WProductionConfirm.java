package de.soundbytes.form;

import org.compiere.model.GridTable;

public class WProductionConfirm extends WAbstractProductionConfirm {

	@Override
	protected void beforeComplete() {
		GridTable gridTable = tab.getTableModel();
        log.warning(gridTable.toString());
	}

}
