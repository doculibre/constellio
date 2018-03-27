package com.constellio.app.ui.pages.management.bagInfo.DisplayBagInfo;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.wrappers.User;

public class DisplayBagInfoPresenter extends BasePresenter<DisplayBagInfoView> {
    private SchemaPresenterUtils utils;

    public DisplayBagInfoPresenter(DisplayBagInfoView view) {
        super(view);
        utils = new SchemaPresenterUtils(BagInfo.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return getCurrentUser().has(RMPermissionsTo.MANAGE_BAG_INFO).globally();
    }

    protected RecordVO getRecordVO(String id) {
        return new RecordToVOBuilder().build(utils.getRecord(id), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
    }
}
