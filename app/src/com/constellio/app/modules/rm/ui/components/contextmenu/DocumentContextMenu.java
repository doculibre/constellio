package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsComponent;
import com.constellio.app.ui.entities.ContentVersionVO;

public interface DocumentContextMenu extends DocumentActionsComponent {

	void setVisible(boolean visible);

	void setContentVersionVO(ContentVersionVO contentVersionVO);

	void buildMenuItems();

}
