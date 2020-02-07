package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDisplayFactory.RetentionRuleDisplayPresenter;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DisplayRetentionRulePresenter extends SingleSchemaBasePresenter<DisplayRetentionRuleView>
		implements RetentionRuleDisplayPresenter {
	private RetentionRuleVO retentionRuleVO;
	private transient DecommissioningService decommissioningService;
	private Record currentRetentionRuleRecord;

	public DisplayRetentionRulePresenter(DisplayRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
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
					.setTitle((String) record.get(
							Schemas.TITLE)).setCode((String) record.get(Schemas.CODE));
			returnList.add(variableRetentionPeriodVO);
		}
		return returnList;
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
		return isManageRetentionRulesGlobally() || user.has(RMPermissionsTo.CONSULT_RETENTIONRULE).globally();
	}


	public boolean isConsultRetentionRuleGlobally() {
		return getCurrentUser().has(RMPermissionsTo.CONSULT_RETENTIONRULE).globally();
	}


	public boolean isManageRetentionRulesGlobally() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}

	private boolean areDocumentRetentionRulesEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}

	//	public List<RecordVODataProvider> getDataProviders(){
	//		MetadataSchemaTypes schemaTypes = modelLayerFactory.getMetadataSchemasManager()
	//				.getSchemaTypes(decommissioningService.getCollection());
	//		List<RecordVODataProvider> dataProviders = currentRetentionRuleRecord.getC
	//	}

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
