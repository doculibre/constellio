package com.constellio.app.ui.pages.management.bagInfo.AddEditBagInfo;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddEditBagInfoPresenter extends BasePresenter<AddEditBagInfoView> {
	RecordServices recordServices;
	AddEditBagInfoView view;
	SchemaPresenterUtils presenterUtils;

	private static Logger LOGGER = LoggerFactory.getLogger(AddEditBagInfoPresenter.class);

	public AddEditBagInfoPresenter(AddEditBagInfoView view) {
		super(view);
		recordServices = recordServices();
		presenterUtils = new SchemaPresenterUtils(BagInfo.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
		this.view = view;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_BAG_INFO).globally();
	}

	public RecordVO getRecordVO(String id) throws RecordServicesRuntimeException.NoSuchRecordWithId {
		return new RecordToVOBuilder().build(presenterUtils.getRecord(id), RecordVO.VIEW_MODE.FORM, this.view.getSessionContext());
	}

	public RecordVO newRecordVO() {
		return new RecordToVOBuilder().build(presenterUtils.newRecord(), RecordVO.VIEW_MODE.FORM, this.view.getSessionContext());
	}

	public void saveButtonClicked(RecordVO recordVO) throws RecordServicesException {
		try {
			Record record = presenterUtils.toRecord(recordVO);
			Transaction trans = new Transaction();
			trans.update(record);
			presenterUtils.recordServices().execute(trans);
			view.navigate().to().previousView();
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}

	}
}
