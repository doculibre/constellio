package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SearchSchemaRecordsPresenter extends SingleSchemaBasePresenter<SearchSchemaRecordsView> {

	public static final String QUERY = "q";
	public static final String SCHEMA_CODE = "schemaCode";
	private String queryExpression;
	private String schemaCode;

	public SearchSchemaRecordsPresenter(SearchSchemaRecordsView view) {
		super(view);
	}

	public void forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		schemaCode = params.get(SCHEMA_CODE);
		setSchemaCode(schemaCode);
		queryExpression = params.get(QUERY);
	}

	public RecordVODataProvider getDataProvider() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(defaultSchema()).returnAll())
						.filteredByStatus(StatusFilter.ACTIVES).sortAsc(Schemas.TITLE)
						.setFreeTextQuery(queryExpression);
			}
		};
		return dataProvider;
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to().displaySchemaRecord(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigate().to().editSchemaRecord(schemaCode, recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		ValidationErrors validationErrors = validateDeletable(recordVO);
		if (validationErrors.isEmpty()) {
			Record record = getRecord(recordVO.getId());
			try {
				delete(record, false);
			} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord error) {
			/*
				This catch happens to avoid presenting a message in the UI
				which wrongly tells the user that the deletion completely failed
				while it really succeeded, but only logically.
			 */
			}
			view.refreshTable();
		} else {
			view.showErrorMessage($("ListSchemaRecordsView.cannotDelete"));
		}

	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
		schemaCode = paramsMap.get(SCHEMA_CODE);
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

	public void search(String freeText) {
		view.navigate().to().searchSchemaRecords(schemaCode, freeText);
	}

	public String getQueryExpression() {
		return queryExpression;
	}
}
