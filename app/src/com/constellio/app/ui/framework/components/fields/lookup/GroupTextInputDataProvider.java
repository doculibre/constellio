package com.constellio.app.ui.framework.components.fields.lookup;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;

public class GroupTextInputDataProvider extends RecordTextInputDataProvider {

	SchemasRecordsServices core;

	public GroupTextInputDataProvider(ConstellioFactories constellioFactories,
									  SessionContext sessionContext) {
		super(constellioFactories, sessionContext, Group.SCHEMA_TYPE, false);
		init();
	}

	private void init() {
		this.core = new SchemasRecordsServices(sessionContext.getCurrentCollection(),
				getModelLayerFactory());
	}

	@Override
	public SPEQueryResponse searchAutocompleteField(User user, String text, int startIndex, int count) {
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(getCurrentCollection()).getSchemaType(Group.SCHEMA_TYPE);


		List<String> globalGroupCode = getModelLayerFactory().newUserServices().getActiveGroups().stream().filter((group -> group
				.getCollections().contains(getCurrentCollection())))
				.map(group -> group.getCode()).collect(Collectors.toList());


		if (globalGroupCode.isEmpty()) {
			return new SPEQueryResponse(Collections.emptyList(), 0);
		}

		LogicalSearchCondition condition;
		if (StringUtils.isNotBlank(text)) {
			condition = from(type).where(autocompleteFieldMatching(text)).andWhere(core.group.code()).isIn(globalGroupCode);
		} else {
			condition = from(type).where(core.group.code()).isIn(globalGroupCode);
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.setStartRow(startIndex)
				.setNumberOfRows(count);
		if (security) {
			if (writeAccess) {
				query.filteredWithUserWrite(user);
			} else {
				query.filteredWithUser(user);
			}
		}
		query.filteredByStatus(StatusFilter.ACTIVES);
		return getModelLayerFactory().newSearchServices().query(query);
	}
}
