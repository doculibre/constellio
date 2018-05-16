package com.constellio.app.ui.pages.management.capsule.addEdit;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;

public class AddEditCapsulePresenter extends BasePresenter<AddEditCapsuleView> {

    private SchemaPresenterUtils utils;

    public AddEditCapsulePresenter(AddEditCapsuleView view) {
        super(view);
        utils = new SchemaPresenterUtils(Capsule.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally();
    }

    public RecordVO getRecordVO(String id) {
        return new RecordToVOBuilder().build(utils.getRecord(id), RecordVO.VIEW_MODE.FORM, view.getSessionContext());

    }

    public RecordVO newRecordVO(){
        return new RecordToVOBuilder().build(utils.newRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
    }

    public void saveButtonClicked(RecordVO recordVO) throws RecordServicesException {
        Record record = utils.toRecord(recordVO);
        Transaction trans = new Transaction();
        trans.update(record);
        utils.recordServices().execute(trans);
        view.navigate().to().previousView();
    }

	public void cancelButtonClicked() {
		view.navigate().to().listCapsule();
	}
}
