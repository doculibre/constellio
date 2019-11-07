package com.constellio.app.modules.rm.ui.components.menuBar;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponent;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public interface DocumentMenuBar extends DocumentActionsComponent {

	void setVisible(boolean visible);

	void setContentVersionVO(ContentVersionVO contentVersionVO);

	void buildMenuItems();
	
	boolean isInViewer();

	void displayInWindow() throws UserDoesNotHaveAccessException;
	
	void editInWindow();

}
