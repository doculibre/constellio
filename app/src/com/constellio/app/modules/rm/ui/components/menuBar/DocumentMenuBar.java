package com.constellio.app.modules.rm.ui.components.menuBar;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponent;
import com.constellio.app.ui.entities.ContentVersionVO;

public interface DocumentMenuBar extends DocumentActionsComponent {

	void setVisible(boolean visible);

	void setContentVersionVO(ContentVersionVO contentVersionVO);

	void buildMenuItems();
	
	boolean isInViewer();
	
	void displayInWindow();
	
	void editInWindow();

}
