package com.constellio.app.modules.rm.services.actions.behaviors;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.wrappers.User;

import java.util.Map;

public abstract class RecordActionBehaviorParams {

	public abstract BaseView getView();

	public abstract RecordVO getRecordVO();

	public abstract ContentVersionVO getContentVersionVO();

	public abstract Map<String, String> getFormParams();

	public abstract User getUser();

	public abstract boolean isContextualMenu();

}
