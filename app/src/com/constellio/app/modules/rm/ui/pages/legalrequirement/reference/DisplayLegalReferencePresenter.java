package com.constellio.app.modules.rm.ui.pages.legalrequirement.reference;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.legalrequirement.requirement.DisplayLegalRequirementView;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;

import java.util.ArrayList;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayLegalReferencePresenter extends SingleSchemaBasePresenter<DisplayLegalRequirementView> {

	private RMSchemasRecordsServices rm;
	private RecordDeleteServices recordDeleteServices;

	private LegalReference legalReference;

	public DisplayLegalReferencePresenter(DisplayLegalRequirementView view) {
		super(view);

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordDeleteServices = new RecordDeleteServices(appLayerFactory.getModelLayerFactory());
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);

		String recordId = paramsMap.get("id");
		legalReference = rm.getLegalReference(recordId);
	}

	public RecordVO getRecordVO() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		return builder.build(legalReference.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void editButtonClicked() {
		if (!canEdit()) {
			return;
		}

		view.navigate().to(RMViews.class).addEditLegalReference(legalReference.getId());
	}

	public void deleteButtonClicked() {
		if (!canEdit()) {
			return;
		}

		if (recordDeleteServices.isReferencedByOtherRecords(legalReference.getWrappedRecord(), new ArrayList<>())) {
			view.showErrorMessage($("LegalRequirementManagement.recordHasReference"));
			return;
		}

		recordDeleteServices.physicallyDeleteNoMatterTheStatus(legalReference.getWrappedRecord(), getCurrentUser(),
				new RecordPhysicalDeleteOptions());

		view.navigate().to().previousView();
	}

	public boolean canEdit() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_LEGAL_REQUIREMENTS).onSomething();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_LEGAL_REQUIREMENTS).onSomething()
			   || user.has(RMPermissionsTo.CONSULT_LEGAL_REQUIREMENTS).onSomething();
	}
}
