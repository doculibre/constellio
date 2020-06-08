package com.constellio.app.ui.pages.trash;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.TrashViewGroup;

public interface TrashView extends BaseView, TrashViewGroup {
	String getSelectedType();

	void enableOrDisableActionButtons();

	void updateSelectDeselectAllToggle(boolean allItemsSelected);
}
