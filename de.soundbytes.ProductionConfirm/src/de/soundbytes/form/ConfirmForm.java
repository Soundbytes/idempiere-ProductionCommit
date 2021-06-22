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

import org.compiere.process.ProcessInfo;
import org.compiere.util.Trx;

/**
 * Generic custom form base class
 * 
 */
public abstract class ConfirmForm
{
	private String title;

	private Trx trx;
	private ProcessInfo pi;
	
	/** User selection */
	public void dynInit() throws Exception {}
	
	public void validate() {}

	public String confirm() {
		return null;
	}
	
	public void executeQuery() {}

	public Trx getTrx() {
		return trx;
	}

	public void setTrx(Trx trx) {
		this.trx = trx;
	}

	public ProcessInfo getProcessInfo() {
		return pi;
	}

	public void setProcessInfo(ProcessInfo pi) {
		this.pi = pi;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
