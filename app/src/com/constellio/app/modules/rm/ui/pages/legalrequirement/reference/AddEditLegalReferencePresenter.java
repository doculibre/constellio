package com.constellio.app.modules.rm.ui.pages.legalrequirement.reference;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AddEditLegalReferencePresenter extends BasePresenter<AddEditLegalReferenceView> {

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;

	private LegalReference legalReference = null;
	private boolean isAddMode;

	public AddEditLegalReferencePresenter(AddEditLegalReferenceView view) {
		super(view);

		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);

		String recordId = paramsMap.get("id");
		if (StringUtils.isNotBlank(recordId)) {
			legalReference = rm.getLegalReference(recordId);
		}

		isAddMode = legalReference == null;

		if (isAddMode) {
			legalReference = rm.newLegalReference();
		}
	}

	public RecordVO getRecordVO() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		return builder.build(legalReference.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	private void fromRecordVO(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(legalReference.getSchemaCode(),
				view.getConstellioFactories(), view.getSessionContext());
		schemaPresenterUtils.fillRecordUsingRecordVO(legalReference.getWrappedRecord(), recordVO, false);
	}

	public void saveButtonClicked(RecordVO recordVO) throws ValidationException, RecordServicesException {
		fromRecordVO(recordVO);

		ValidationErrors validationErrors = new ValidationErrors();
		validateRecord(validationErrors);
		validationErrors.throwIfNonEmpty();

		Transaction tr = new Transaction();
		tr.addUpdate(legalReference.getWrappedRecord());

		recordServices.execute(tr);
		view.navigate().to().previousView();
	}

	private void validateRecord(ValidationErrors validationErrors) {
		if (StringUtils.isBlank(legalReference.getTitle())) {
			validationErrors.add(AddEditLegalReferencePresenter.class, "titleRequired");
		}
	}

	public void cancelButtonClicked() {
		view.navigate().to().previousView();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_LEGAL_REQUIREMENTS).onSomething();
	}
}
