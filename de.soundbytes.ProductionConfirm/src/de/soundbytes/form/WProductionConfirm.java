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

package de.soundbytes.form;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.webui.ClientInfo;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.adwindow.AbstractADWindowContent;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.model.GridTab;
import org.compiere.model.GridTabVO;
import org.compiere.model.GridTable;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MProduction;
import org.compiere.model.MProductionLine;
import org.compiere.model.MOrg;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.wf.MWorkflow;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import de.soundbytes.utils.Util;
import org.adempiere.webui.adwindow.StatusBar;

public class WProductionConfirm extends ConfirmForm implements IFormController, EventListener<Event> {

	private WQuickADFormProdConfirm form;
	
	/**	Logger			*/
	protected static CLogger log = CLogger.getCLogger(WProductionConfirm.class);
	
	private Label productionLabel = new Label();
	private Listbox productionListbox = ListboxFactory.newDropdownListbox();
	private Label   docActionLabel = new Label();
	private WTableDirEditor docActionEditor;
	private Checkbox keepOpenCheckbox = new Checkbox();
	protected GridTab tab;

	private int productionID = 0;
	private ArrayList<Integer> prodIDs = null;
	private int noOfParameterCols;
	private boolean init = true;
	private String documentNo;


	public WProductionConfirm()
	{
		Component activeWindow = SessionManager.getAppDesktop().getActiveWindow();
		ADWindowContent windowContent = ADWindow.findADWindow(activeWindow).getADWindowContent();

		windowContent.setStatusBarQF(new StatusBar());
		tab = windowContent.getActiveGridTab();
		form = new WQuickADFormProdConfirm(windowContent, false, 0);		

		try {
			super.dynInit();
			dynInit();
			zkInit();
		}
		catch(Exception ex) {
			log.log(Level.SEVERE, "init", ex);
		}
		ClientInfo.onClientInfo(form, this::onClientInfo);
	}	//	init
	
	public void dynInit() throws Exception
	{
		setTitle("Production Confirmation");
		
		productionLabel.setText(Msg.translate(Env.getCtx(), "M_Production_ID"));

		// Document Action Complete/Close
		docActionLabel.setText(Msg.translate(Env.getCtx(), "DocAction"));
		MLookup docActionL = MLookupFactory.get(Env.getCtx(), form.getWindowNo(), 210983 /* M_Production.DocAction */,
				DisplayType.List, Env.getLanguage(Env.getCtx()), "DocAction", 135 /* _Document Action */,
				false, "AD_Ref_List.Value IN ('CO','CL','--')");
		docActionEditor = new WTableDirEditor("DocAction", true, false, true, docActionL);
		docActionEditor.setValue(DocAction.ACTION_Complete);
	}
	
	private boolean reloadProductionListbox() {
		ArrayList<KeyNamePair> productions = loadProductions();
		productionListbox.getItems().clear();
		for(KeyNamePair knp : productions) {
			productionListbox.addItem(knp);
		}
		boolean needLoadLines = true;
		int prodID = (Integer)tab.getValue("M_Production_ID");
		if (MProductionLine.Table_Name.equals(tab.getTableName()) && prodIDs.contains(prodID)) 
			needLoadLines = false;
		else
			prodID = 0;
		
		if (init) {
			keepOpenCheckbox.setChecked(!(prodID > 0 || productions.size() <= 1));
			init = false;
		}else if ( productions.size() <= 1)
			keepOpenCheckbox.setChecked(false);
			
		if (prodID < 1 && !prodIDs.isEmpty())
			prodID = prodIDs.get(0);
		
		productionListbox.setValue(prodID);
		boolean retVal = setProductionID(prodID);

		if (needLoadLines)
			loadLines();

		return retVal;
	}
	
	private boolean setProductionID(int prodID) {
		productionID = prodID;
		if (prodID > 0) { 
			MProduction prod = new MProduction(Env.getCtx(), productionID, null);
			documentNo = prod.getDocumentNo();
			String prodName = prod.getName();
			String moveDate = DisplayType.getDateFormat(DisplayType.Date, Env.getLanguage(Env.getCtx())).format(prod.getMovementDate());
			
			StringBuilder status = Util.isStrEmpty(prodName) ? new StringBuilder ()
					: new StringBuilder (prodName).append("    |    ");
			status.append("Production: ")
					.append(documentNo)
					.append("    |    Organization: ")
					.append(new MOrg(Env.getCtx(), prod.getAD_Org_ID(), null).getName())
					.append("    |    Move Date: ")
					.append(moveDate);
			form.getStatusBar().setInfo(status.toString());
			return true;
		}
		else {
			form.getStatusBar().setInfo("No open Productions found. Please close the dialog.");
			return false;
		}
	}

	void zkInit() {
		keepOpenCheckbox.setLabel("Keep Dialog Opened");
		form.getConfirmPanel().addComponentsCenter(keepOpenCheckbox);
		
		setupColumns();

		Rows rows = form.getParameterPanel().newRows();
		Row row = rows.newRow();
		rows.appendChild(row);
		row.appendCellChild(productionLabel.rightAlign());
		ZKUpdateUtil.setHflex(productionListbox, "true");
		row.appendCellChild(productionListbox);
		
		row.appendCellChild(docActionLabel.rightAlign());
		ZKUpdateUtil.setHflex(docActionEditor.getComponent(), "true");		
		row.appendCellChild(docActionEditor.getComponent());
		
		if (noOfParameterCols < 6)
			LayoutUtils.compactTo(form.getParameterPanel(), noOfParameterCols);
		else
			LayoutUtils.expandTo(form.getParameterPanel(), noOfParameterCols, true);	
		
		if (ThemeManager.isUseCSSForWindowSize()) {
		    ZKUpdateUtil.setWindowHeightX(form, 400);
		    ZKUpdateUtil.setWindowWidthX(form, 1200);
		}
		form.setSclass("popup-dialog text-editor-dialog");
		form.setStyle("position: absolute;");
		form.setStyle("top: 100px;");
		
		reloadProductionListbox();
		productionListbox.addActionListener(this);
	}
	
	private void setupColumns() {
		noOfParameterCols = 4;
		if (ClientInfo.maxWidth(ClientInfo.MEDIUM_WIDTH-1))
		{
			if (ClientInfo.maxWidth(ClientInfo.SMALL_WIDTH-1))
				noOfParameterCols = 2;
			else
				noOfParameterCols = 4;
		}
		if (noOfParameterCols == 2)
		{
			Columns columns = new Columns();
			Column column = new Column();
			column.setWidth("35%");
			columns.appendChild(column);
			column = new Column();
			column.setWidth("65%");
			columns.appendChild(column);
			form.getParameterPanel().appendChild(columns);
		}
	}	
	
	private ArrayList<KeyNamePair> loadProductions()
	{
		ArrayList<KeyNamePair> list = new ArrayList<KeyNamePair>();
		prodIDs = new ArrayList<Integer>();
		
		Properties ctx = Env.getCtx();
		int adClientID = Env.getAD_Client_ID(ctx);
		int adOrgID = Env.getAD_Org_ID(ctx);	
		
		//
		StringBuffer sql = new StringBuffer("SELECT p.M_Production_ID, p.DocumentNo")
			.append(" FROM M_Production p WHERE p.AD_Client_ID = ?");
		if (adOrgID > 0) 
			sql.append(" AND p.AD_Org_ID = ?");
			sql.append(" AND p.DocStatus NOT IN ('CL','CO')");
		
		sql.append(" ORDER BY p.DatePromised");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, adClientID);
			if (adOrgID > 0)
				pstmt.setInt(2, adOrgID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
				prodIDs.add(rs.getInt(1));
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return list;
	}		
	
	@Override
	public ADForm getForm() {
		return form;
	}

	private void onClientInfo()
	{
		if (ClientInfo.isMobile() && form.getPage() != null) 
		{
			if (noOfParameterCols > 0 && form.getParameterPanel().getRows() != null)
			{
				int t = 6;
				if (ClientInfo.maxWidth(ClientInfo.MEDIUM_WIDTH-1))
				{
					if (ClientInfo.maxWidth(ClientInfo.SMALL_WIDTH-1))
						t = 2;
					else
						t = 4;
				}
				if (t != noOfParameterCols)
				{
					form.getParameterPanel().getRows().detach();
					if (form.getParameterPanel().getColumns() != null)
						form.getParameterPanel().getColumns().detach();
					try {
						zkInit();
						form.invalidate();
					} catch (Exception e1) {}
				}
			}
		}
	}
	
	/**
	 *	Action Listener
	 *  @param e event
	 */
	public void onEvent(Event e)
	{
		if (log.isLoggable(Level.INFO)) log.info("Cmd=" + e.getTarget().getId());
		//
		if(productionListbox.equals(e.getTarget())) {
			setProductionID((int)productionListbox.getSelectedItem().getValue());
			loadLines();
		}
	}	//	actionPerformed

	private void loadLines() {
		GridTabVO vo = tab.getVO();
		Env.setContext(vo.ctx, vo.WindowNo, 0, "M_Production_ID", productionID);
        tab.query(false, 0, 0);
    	tab.getTableModel().fireTableDataChanged();	
    }

	public void validate()
	{
		if (productionListbox.getItemCount() == 0) {
			dispose();
			return;
		}	

		if ( Util.fieldToInt(productionListbox.getValue()) == 0)
			throw new WrongValueException(productionListbox, Msg.translate(Env.getCtx(), "FillMandatory"));

		String docActionSelected = (String)docActionEditor.getValue();
		if ( docActionSelected==null || docActionSelected.isEmpty() )
			throw new WrongValueException(docActionEditor.getComponent(), Msg.translate(Env.getCtx(), "FillMandatory"));

		// TODO: the data processing really shouldn't be done by the UI thread
		String status = confirm();
		
		if (keepOpenCheckbox.isChecked() && status != null) {
			form.getStatusBar().setStatusLine(status);
			reloadProductionListbox();
		}
		else
			dispose();
	}

	/**************************************************************************
	 *	Production Confirm
	 */
	@Override
	public String confirm()
	{
		Properties ctx = Env.getCtx();
		
		setProcessInfo(new ProcessInfo("ProductionConfirm", 328625));
		String trxName = Trx.createTrxName("PrCo");
		Trx trx = Trx.get(trxName, true);	//trx needs to be committed too
		trx.setDisplayName("Production Confirm");
		
		StringBuilder status = null;
		
		String docActionSelected = (String)docActionEditor.getValue();
		if (DocAction.ACTION_Complete.equals(docActionSelected) || DocAction.ACTION_Close.equals(docActionSelected)) {
			beforeComplete();
			status = new StringBuilder("Production ")
					.append(documentNo);
			MProduction production = new MProduction(ctx, productionID, trxName);
			MWorkflow.runDocumentActionWorkflow(production, DocAction.ACTION_Complete);
			if (DocAction.ACTION_Close.equals(docActionSelected)) {
				MWorkflow.runDocumentActionWorkflow(production, DocAction.ACTION_Close);
				status.append(" closed");
			}
			else
				status.append(" completed");
		}
		trx.commit();
		trx.close();
		
		return status == null ? null : status.toString();
	}	//	Movement Confirm
	
	protected void beforeComplete() {
		GridTable gridTable = tab.getTableModel();
        log.warning(gridTable.toString());
	}

	void dispose() {
		Clients.clearBusy();
		form.dispose();
	}
	

	// is there a better way to intercept the forms event handling than subclassing WQuickADForm?
	class WQuickADFormProdConfirm extends WQuickADForm{
		private static final long serialVersionUID = -3302108060544639901L;

		public WQuickADFormProdConfirm(AbstractADWindowContent winContent, boolean m_onlyCurrentRows,
				int m_onlyCurrentDays) {
			super(winContent, m_onlyCurrentRows, m_onlyCurrentDays);
		}
		
		@Override
		public void onEvent(Event event) throws Exception {
			if (event.getTarget() == getConfirmPanel().getButton(ConfirmPanel.A_OK))
			{
				onSave();
				validate();
			} else {
				super.onEvent(event);
			}
		}
		
	}
}
