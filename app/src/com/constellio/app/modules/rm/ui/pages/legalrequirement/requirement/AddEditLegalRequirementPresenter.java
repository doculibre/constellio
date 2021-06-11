package com.constellio.app.modules.rm.ui.pages.legalrequirement.requirement;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.legalrequirement.component.LegalRequirementReferenceEditableRecordTablePresenter;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
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

public class AddEditLegalRequirementPresenter extends BasePresenter<AddEditLegalRequirementView> {

	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;

	private LegalRequirement legalRequirement = null;
	private LegalRequirementReferenceEditableRecordTablePresenter requirementReferencePresenter;
	private boolean isAddMode;

	public AddEditLegalRequirementPresenter(AddEditLegalRequirementView view) {
		super(view);

		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);

		String recordId = paramsMap.get("id");
		if (StringUtils.isNotBlank(recordId)) {
			legalRequirement = rm.getLegalRequirement(recordId);
		}

		isAddMode = legalRequirement == null;

		if (isAddMode) {
			legalRequirement = rm.newLegalRequirement();
		}

		requirementReferencePresenter = new LegalRequirementReferenceEditableRecordTablePresenter(appLayerFactory,
				view.getSessionContext(), legalRequirement.getId());
	}

	public RecordVO getRecordVO() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		return builder.build(legalRequirement.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	public LegalRequirementReferenceEditableRecordTablePresenter getRequirementReferenceFieldPresenter() {
		return requirementReferencePresenter;
	}

	private void fromRecordVO(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(legalRequirement.getSchemaCode(),
				view.getConstellioFactories(), view.getSessionContext());
		schemaPresenterUtils.fillRecordUsingRecordVO(legalRequirement.getWrappedRecord(), recordVO, false);
	}

	public void saveButtonClicked(RecordVO recordVO) throws ValidationException, RecordServicesException {
		fromRecordVO(recordVO);

		ValidationErrors validationErrors = new ValidationErrors();
		validateRecord(validationErrors);
		validationErrors.throwIfNonEmpty();

		Transaction tr = new Transaction();
		tr.addUpdate(legalRequirement.getWrappedRecord());
		tr.addUpdate(requirementReferencePresenter.getRecordsToPersist());

		requirementReferencePresenter.deleteRemovedRecords();
		recordServices.execute(tr);
		view.navigate().to().previousView();
	}

	public boolean isRequirementsReferenceDirty() {
		return requirementReferencePresenter.isDirty();
	}

	private void validateRecord(ValidationErrors validationErrors) {
		if (StringUtils.isBlank(legalRequirement.getTitle())) {
			validationErrors.add(AddEditLegalRequirementPresenter.class, "titleRequired");
		}

		if (StringUtils.isBlank(legalRequirement.getCode())) {
			validationErrors.add(AddEditLegalRequirementPresenter.class, "codeRequired");
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
