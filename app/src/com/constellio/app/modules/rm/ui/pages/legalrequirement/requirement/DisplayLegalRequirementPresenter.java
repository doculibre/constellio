package com.constellio.app.modules.rm.ui.pages.legalrequirement.requirement;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.legalrequirement.component.LegalRequirementReferenceEditableRecordTablePresenter;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DisplayLegalRequirementPresenter extends SingleSchemaBasePresenter<DisplayLegalRequirementView> {

	private RMSchemasRecordsServices rm;
	private RecordDeleteServices recordDeleteServices;
	private SearchServices searchServices;

	private LegalRequirement legalRequirement;

	public DisplayLegalRequirementPresenter(DisplayLegalRequirementView view) {
		super(view);

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordDeleteServices = new RecordDeleteServices(appLayerFactory.getModelLayerFactory());
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public void forParams(String params) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);

		String recordId = paramsMap.get("id");
		legalRequirement = rm.getLegalRequirement(recordId);
	}

	public RecordVO getRecordVO() {
		RecordToVOBuilder builder = new RecordToVOBuilder();
		return builder.build(legalRequirement.getWrappedRecord(), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public LegalRequirementReferenceEditableRecordTablePresenter getRequirementReferenceFieldPresenter() {
		return new LegalRequirementReferenceEditableRecordTablePresenter(appLayerFactory, view.getSessionContext(),
				legalRequirement.getId());
	}

	public void editButtonClicked() {
		if (!canEdit()) {
			return;
		}

		view.navigate().to(RMViews.class).addEditLegalRequirement(legalRequirement.getId());
	}

	public void deleteButtonClicked() {
		if (!canEdit()) {
			return;
		}

		LogicalSearchCondition condition = from(rm.legalRequirementReference.schemaType())
				.where(rm.legalRequirementReference.ruleRequirement()).isEqualTo(legalRequirement.getId());
		List<Record> records = searchServices.search(new LogicalSearchQuery(condition));

		if (recordDeleteServices.isReferencedByOtherRecords(legalRequirement.getWrappedRecord(), records)) {
			view.showErrorMessage($("LegalRequirementManagement.recordHasReference"));
			return;
		}

		for (Record record : records) {
			recordDeleteServices.physicallyDeleteNoMatterTheStatus(record, getCurrentUser(),
					new RecordPhysicalDeleteOptions());
		}

		recordDeleteServices.physicallyDeleteNoMatterTheStatus(legalRequirement.getWrappedRecord(), getCurrentUser(),
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

	public SelectionPanelReportPresenter getSelectionPanelReportPresenter() {
		return new SelectionPanelReportPresenter(appLayerFactory, collection, getCurrentUser()) {
			@Override
			public String getSelectedSchemaType() {
				return LegalRequirement.SCHEMA_TYPE;
			}

			@Override
			public List<String> getSelectedRecordIds() {
				return asList(legalRequirement.getId());
			}
		};
	}
}
