package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
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
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DisplayRetentionRulePresenter extends SingleSchemaBasePresenter<DisplayRetentionRuleView> {
	private RetentionRuleVO retentionRuleVO;
	private transient DecommissioningService decommissioningService;

	public DisplayRetentionRulePresenter(DisplayRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		decommissioningService = new DecommissioningService(collection, modelLayerFactory);
	}

	public void forParams(String params) {
		Record record = getRecord(params);
		retentionRuleVO = new RetentionRuleToVOBuilder(appLayerFactory, types().getDefaultSchema(Category.SCHEMA_TYPE),
				schema(UniformSubdivision.DEFAULT_SCHEMA))
				.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setRetentionRule(retentionRuleVO);
	}

	public void viewAssembled() {
	}

	public void backButtonClicked() {
		view.navigateTo().listRetentionRules();
	}

	public void editButtonClicked() {
		view.navigateTo().editRetentionRule(retentionRuleVO.getId());
	}

	public void deleteButtonClicked() {
		Record record = getRecord(retentionRuleVO.getId());
		delete(record, false);
		view.navigateTo().listRetentionRules();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}

	public String getFoldersNumber() {
		return String.valueOf(decommissioningService.getFoldersForRetentionRule(retentionRuleVO.getId()).size());
	}

	@Override
	public boolean isDeletable(RecordVO entity) {
		RecordServices recordService = modelLayerFactory.newRecordServices();
		Record record = getRecord(entity.getId());
		User user = getCurrentUser();
		return recordService.isLogicallyDeletable(record, user);
	}

	public List<VariableRetentionPeriodVO> getOpenActivePeriodsDDVList() {
		List<String> variablePeriodCodes = new ArrayList<>();
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
}
