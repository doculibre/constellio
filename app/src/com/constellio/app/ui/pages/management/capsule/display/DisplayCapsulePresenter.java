package com.constellio.app.ui.pages.management.capsule.display;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;

public class DisplayCapsulePresenter extends BasePresenter<DisplayCapsuleView> {

	private SchemaPresenterUtils utils;

	public DisplayCapsulePresenter(DisplayCapsuleView view) {
		super(view);
		utils = new SchemaPresenterUtils(Capsule.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally();
	}

	public RecordVO getRecordVO(String id) {
		return new RecordToVOBuilder().build(utils.getRecord(id), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
	}
}
