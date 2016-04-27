package com.constellio.app.modules.robots.model.wrappers;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Robot extends RecordWrapper {
	public static final String SCHEMA_TYPE = "robot";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String SCHEMA_FILTER = "schemaFilter";
	public static final String SEARCH_CRITERIA = "searchCriteria";
	public static final String ACTION = "action";
	public static final String ACTION_PARAMETERS = "actionParameters";
	public static final String EXCLUDE_PROCESSED_BY_CHILDREN = "excludeProcessedByChildren";
	public static final String AUTO_EXECUTE = "autoExecute";

	public Robot(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Robot setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public Robot setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Robot setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public Robot setParent(String parentId) {
		set(PARENT, parentId);
		return this;
	}

	public Robot setParent(Record parent) {
		set(PARENT, parent);
		return this;
	}

	public Robot setParent(Robot parent) {
		set(PARENT, parent);
		return this;
	}

	public String getSchemaFilter() {
		return get(SCHEMA_FILTER);
	}

	public Robot setSchemaFilter(String schemaType) {
		set(SCHEMA_FILTER, schemaType);
		return this;
	}

	public List<Criterion> getSearchCriteria() {
		return getList(SEARCH_CRITERIA);
	}

	public Robot setSearchCriterion(CriterionBuilder criterion) {
		return setSearchCriterion(criterion.build());
	}

	public Robot setSearchCriterion(Criterion criterion) {
		return setSearchCriteria(asList(criterion));
	}

	public Robot setSearchCriteria(List<Criterion> criteria) {
		set(SEARCH_CRITERIA, criteria);
		return this;
	}

	public String getAction() {
		return get(ACTION);
	}

	public Robot setAction(String action) {
		set(ACTION, action);
		return this;
	}

	public String getActionParameters() {
		return get(ACTION_PARAMETERS);
	}

	public Robot setActionParameters(String parametersId) {
		set(ACTION_PARAMETERS, parametersId);
		return this;
	}

	public Robot setActionParameters(ActionParameters parameters) {
		set(ACTION_PARAMETERS, parameters);
		return this;
	}

	public boolean getExcludeProcessedByChildren() {
		return get(EXCLUDE_PROCESSED_BY_CHILDREN);
	}

	public Robot setExcludeProcessedByChildren(boolean excludeProcessed) {
		set(EXCLUDE_PROCESSED_BY_CHILDREN, excludeProcessed);
		return this;
	}

	public boolean isAutoExecute() {
		return getBooleanWithDefaultValue(AUTO_EXECUTE, false);
	}

	public Robot setAutoExecute(boolean autoExecute) {
		set(AUTO_EXECUTE, autoExecute);
		return this;
	}

	public boolean isRoot() {
		return getParent() == null;
	}
}
