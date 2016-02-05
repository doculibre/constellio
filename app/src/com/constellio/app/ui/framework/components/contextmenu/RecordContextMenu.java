package com.constellio.app.ui.framework.components.contextmenu;

import com.constellio.app.ui.entities.RecordVO;

public abstract class RecordContextMenu extends BaseContextMenu {

	public abstract boolean openFor(RecordVO recordVO);
	
	public abstract boolean openFor(String recordId);
	
}
