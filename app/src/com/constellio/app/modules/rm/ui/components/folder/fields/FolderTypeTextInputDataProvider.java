package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;

public class FolderTypeTextInputDataProvider extends RecordTextInputDataProvider {

	private String parent;
	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rm;

	public FolderTypeTextInputDataProvider(ConstellioFactories constellioFactories,
										   SessionContext sessionContext, String parent) {
		super(constellioFactories, sessionContext, FolderType.SCHEMA_TYPE, false);
		this.parent = parent;
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		this.rmConfigs = new RMConfigs(getModelLayerFactory().getSystemConfigurationsManager());
		this.rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(),
				getModelLayerFactory());
	}

	@Override
	public SPEQueryResponse searchAutocompleteField(User user, String text, int startIndex, int count) {
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(getCurrentCollection()).getSchemaType(FolderType.SCHEMA_TYPE);

		LogicalSearchCondition condition;
		if (StringUtils.isNotBlank(text)) {
			condition = from(type).where(autocompleteFieldMatching(text));
		} else {
			condition = from(type).returnAll();
		}

		if (parent != null) {
			Folder parentFolder = rm.getFolder(parent);
			List<String> newFolderTypes = Collections.EMPTY_LIST;
			if (rmConfigs.isTypeRestrictionEnabledInFolder()) {
				newFolderTypes = parentFolder.getAllowedFolderTypes();
			}

			if (!newFolderTypes.isEmpty()) {
				condition = condition.andWhere(Schemas.IDENTIFIER).isIn(newFolderTypes);
			}
		}
		if (onlyLinkables) {
			condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.filteredByStatus(StatusFilter.ACTIVES)
				.setStartRow(startIndex)
				.setNumberOfRows(count);
		if (security) {
			if (writeAccess) {
				query.filteredWithUserWrite(user);
			} else {
				query.filteredWithUserRead(user);
			}
		}
		return getModelLayerFactory().newSearchServices().query(query);
	}
}
