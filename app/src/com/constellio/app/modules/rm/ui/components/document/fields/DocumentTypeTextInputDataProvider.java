package com.constellio.app.modules.rm.ui.components.document.fields;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;

public class DocumentTypeTextInputDataProvider extends RecordTextInputDataProvider {

	private String folderId;
	private String currentType;
	private transient RMConfigs rmConfigs;
	private transient RMSchemasRecordsServices rm;

	public DocumentTypeTextInputDataProvider(ConstellioFactories constellioFactories,
											 SessionContext sessionContext, String folderId, String currentType) {
		super(constellioFactories, sessionContext, DocumentType.SCHEMA_TYPE, false);
		this.folderId = folderId;
		this.currentType = currentType;
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
				.getSchemaTypes(getCurrentCollection()).getSchemaType(DocumentType.SCHEMA_TYPE);

		LogicalSearchCondition condition;
		if (StringUtils.isNotBlank(text)) {
			condition = from(type).where(autocompleteFieldMatching(text));
		} else {
			condition = from(type).returnAll();
		}

		if (folderId != null) {
			List<String> newDocumentTypes = Collections.EMPTY_LIST;
			if (rmConfigs.isTypeRestrictionEnabledInFolder()) {
				newDocumentTypes = rm.getFolder(folderId).getAllowedDocumentTypes();
			}

			if (newDocumentTypes.isEmpty()) {
				newDocumentTypes = getDocumentTypesFilteredByRetentionRule();
			}

			if (!newDocumentTypes.isEmpty()) {
				condition = condition.andWhere(Schemas.IDENTIFIER).isIn(newDocumentTypes);
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
				query.filteredWithUser(user);
			}
		}
		return getModelLayerFactory().newSearchServices().query(query);
	}

	private List<String> getDocumentTypesFilteredByRetentionRule() {
		List<String> newDocumentTypes = new ArrayList<>();
		if (rmConfigs.getDocumentsTypesChoice()
			== DocumentsTypeChoice.FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES
			|| rmConfigs.getDocumentsTypesChoice()
			   == DocumentsTypeChoice.LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES) {
			RetentionRule retentionRule = rm.getRetentionRule(rm.getFolder(folderId).getRetentionRule());
			List<String> documentTypes = retentionRule.getDocumentTypes();
			if (currentType != null && !documentTypes.contains(currentType)) {
				newDocumentTypes.add(currentType);
			}
			newDocumentTypes.addAll(documentTypes);
		}
		return newDocumentTypes;
	}
}
