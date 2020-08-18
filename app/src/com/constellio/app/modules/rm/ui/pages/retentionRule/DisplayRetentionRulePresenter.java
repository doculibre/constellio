package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplayFactory.RetentionRuleDisplayPresenter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class DisplayRetentionRulePresenter extends SingleSchemaBasePresenter<DisplayRetentionRuleView>
		implements RetentionRuleDisplayPresenter {
	private RetentionRuleVO retentionRuleVO;
	private transient DecommissioningService decommissioningService;
	private Record currentRetentionRuleRecord;

	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private SessionContext sessionContext;

	public DisplayRetentionRulePresenter(DisplayRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		sessionContext = view.getSessionContext();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
	}

	public void forParams(String params) {
		currentRetentionRuleRecord = getRecord(params);
		retentionRuleVO = new RetentionRuleToVOBuilder(
				appLayerFactory, types().getDefaultSchema(Category.SCHEMA_TYPE), schema(UniformSubdivision.DEFAULT_SCHEMA))
				.build(currentRetentionRuleRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setRetentionRule(retentionRuleVO);
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).listRetentionRules();
	}

	public void editButtonClicked() {
		view.navigate().to(RMViews.class).editRetentionRule(retentionRuleVO.getId());
	}

	public void deleteButtonClicked() {
		Record record = getRecord(retentionRuleVO.getId());
		delete(record, false);
		view.navigate().to(RMViews.class).listRetentionRules();
	}

	public String getFoldersNumber() {
		return String.valueOf(decommissioningService.getFolderCountForRetentionRule(retentionRuleVO.getId()));
	}

	@Override
	public ValidationErrors validateDeletable(RecordVO entity) {
		RecordServices recordService = modelLayerFactory.newRecordServices();
		Record record = getRecord(entity.getId());
		User user = getCurrentUser();
		return recordService.validateLogicallyDeletable(record, user);
	}

	@Override
	public List<VariableRetentionPeriodVO> getOpenActivePeriodsDDVList() {
		List<String> variablePeriodCodes = new ArrayList<>();

		if(retentionRuleVO.getMetadataOrNull(RetentionRule.COPY_RETENTION_RULES) == null) {
			return Collections.emptyList();
		}

		for (CopyRetentionRule copyRetentionRule : retentionRuleVO.getCopyRetentionRules()) {
			if (!variablePeriodCodes.contains("" + copyRetentionRule.getActiveRetentionPeriod().getValue())) {
				variablePeriodCodes.add("" + copyRetentionRule.getActiveRetentionPeriod().getValue());
			}
		}
		List<VariableRetentionPeriodVO> returnList = new ArrayList<>();
		LogicalSearchCondition condition = from(schemaType(VariableRetentionPeriod.SCHEMA_TYPE).getDefaultSchema())
				.where(Schemas.CODE).isIn(variablePeriodCodes);
		List<Record> records = searchServices().search(new LogicalSearchQuery(condition));
		for (Record record : records) {
			VariableRetentionPeriodVO variableRetentionPeriodVO = new VariableRetentionPeriodVO().setRecordId(record.getId())
					.setTitle((String) record.get(Schemas.TITLE, ConstellioUI.getCurrentSessionContext().getCurrentLocale())).setCode((String) record.get(Schemas.CODE));
			returnList.add(variableRetentionPeriodVO);
		}
		return returnList;
	}

	public void tabElementClicked(RecordVO recordVO) {
		if (recordVO.getSchema().getCode().contains(Folder.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).displayFolder(recordVO.getId());
		}
	}

	@Override
	public boolean shouldDisplayFolderRetentionRules() {
		return !areDocumentRetentionRulesEnabled() || retentionRuleVO.getScope() != RetentionRuleScope.DOCUMENTS;
	}

	@Override
	public boolean shouldDisplayDocumentRetentionRules() {
		return areDocumentRetentionRulesEnabled();
	}

	@Override
	public boolean shouldDisplayDefaultDocumentRetentionRules() {
		return areDocumentRetentionRulesEnabled() && retentionRuleVO.getScope() != RetentionRuleScope.DOCUMENTS_AND_FOLDER;
	}

	@Override
	public boolean shouldDisplayDocumentTypeDetails() {
		return !areDocumentRetentionRulesEnabled();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return isManageRetentionRulesOnSomething() || isConsultRetentionRuleOnSomething();
	}


	public boolean isConsultRetentionRuleOnSomething() {
		return getCurrentUser().has(RMPermissionsTo.CONSULT_RETENTIONRULE).onSomething();
	}


	public boolean isManageRetentionRulesOnSomething() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_RETENTIONRULE).onSomething();
	}

	private boolean areDocumentRetentionRulesEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}

	public RecordVODataProvider getDataProvider() {
		MetadataSchemaType folderSchemaType = rmSchemasRecordsServices.folderSchemaType();
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				folderSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext
		);

		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				List<LogicalSearchCondition> conditions = new ArrayList<>();

				if (currentRetentionRuleRecord != null) {
					conditions.add(getRetentionRuleCondition(retentionRuleVO.getId()));
				}

				return new LogicalSearchQuery(from(folderSchemaType).whereAllConditions(conditions))
						.sortDesc(Schemas.MODIFIED_ON).filteredWithUser(getCurrentUser());
			}

			private LogicalSearchCondition getRetentionRuleCondition(String retentionRule) {
				return where(rmSchemasRecordsServices.folder.retentionRule()).isEqualTo(rmSchemasRecordsServices.getRetentionRule(retentionRule));
			}
		};
	}

	@Override
	public CopyRetentionRule newDocumentCopyRetentionRule() {
		return new CopyRetentionRule();
	}

	@Override
	public CopyRetentionRule newFolderCopyRetentionRule(boolean principal) {
		return new CopyRetentionRule();
	}

	@Override
	public CopyRetentionRule newDocumentDefaultCopyRetentionRule(boolean principal) {
		return new CopyRetentionRule();
	}

	@Override
	public List<VariableRetentionPeriodVO> getOpenPeriodsDDVList() {
		return getOpenActivePeriodsDDVList();
	}
}
