package com.constellio.app.services.menu.behavior;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.wrappers.User;

import java.util.Map;

public abstract class MenuItemActionBehaviorParams {

	public abstract BaseView getView();

	public abstract Map<String, String> getFormParams();

	public abstract User getUser();

	public RecordVO getRecordVO() {
		return null;
	}

	public ContentVersionVO getContentVersionVO() {
		return null;
	}

	public boolean isContextualMenu() {
		return false;
	}

	public boolean isNestedView() {
		return false;
	}

	public Object getObjectRecordVO() {
		return null;
	}

}
