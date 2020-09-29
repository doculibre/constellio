package com.constellio.app.ui.framework.data;

import com.constellio.app.api.extensions.params.RecordTextInputDataProviderSortMetadatasParam;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatchingInMetadatas;

@Builder
public class AutocompleteQuery {

	private AppLayerFactory appLayerFactory;
	private String schemaTypeCode;
	private String schemaCode;
	private boolean hasSecurity;
	private String expression;
	private User user;
	private Boolean writeAccessRequired;
	private int startRow;
	private int rowCount;
	private Locale locale;
	private boolean onlyLinkables;
	private boolean includeDeactivated;
	private boolean includeLogicallyDeleted;
	private List<String> idsToIgnore;
	private ReturnedMetadatasFilter metadataFilter;

	public LogicalSearchQuery searchQuery() {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		String collection = user.getCollection();

		LogicalSearchCondition condition;

		Metadata sort = null;
		if (schemaTypeCode != null) {
			MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
			List<Metadata> extraMetadatas = type.getDefaultSchema().getMetadatas().onlySearchable()
					.onlySchemaAutocomplete().excludingValueTypes(STRUCTURE);
			if (StringUtils.isNotBlank(expression)) {
				condition = from(type).where(autocompleteFieldMatchingInMetadatas(expression, extraMetadatas));
			} else {
				String caption = $("caption." + type.getCode() + ".record");

				condition = from(type).returnAll();
				if (caption != null && caption.startsWith("{code}")) {
					sort = Schemas.CODE;
				} else {
					sort = Schemas.TITLE;
				}
			}

			if (schemaTypeCode.equals(Category.SCHEMA_TYPE) && !includeDeactivated) {
				condition = condition.andWhere(type.getAllMetadatas().getMetadataWithLocalCode(Category.DEACTIVATE))
						.isFalseOrNull();
			}
		} else {
			MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(schemaCode);
			List<Metadata> extraMetadatas = schema.getMetadatas().onlySearchable().onlySchemaAutocomplete();
			if (StringUtils.isNotBlank(expression)) {
				condition = from(schema).where(autocompleteFieldMatchingInMetadatas(expression, extraMetadatas));
			} else {
				condition = from(schema).returnAll();
			}
		}
		if (onlyLinkables) {
			condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
		}

		condition = addIgnoreIdsToCondition(condition);

		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.setPreferAnalyzedFields(true)
				.setStartRow(startRow)
				.setNumberOfRows(rowCount)
				.setName("Autocomplete query for input '" + expression + "'");

		//		boolean isDDV = schemaTypeCode != null? schemaTypeCode.startsWith("ddv"):schemaCode.startsWith("ddv");
		if (!includeLogicallyDeleted) {
			query.filteredByStatus(StatusFilter.ACTIVES);
		}

		RecordTextInputDataProviderSortMetadatasParam sortParams = new RecordTextInputDataProviderSortMetadatasParam(schemaCode, schemaTypeCode, ConstellioUI.getCurrent());
		Metadata[] sortMetadatas = appLayerFactory.getExtensions().forCollection(collection).getRecordTextInputDataProviderSortMetadatas(sortParams);
		if (sortMetadatas != null) {
			for (Metadata sortMetadata : sortMetadatas) {
				query.sortAsc(sortMetadata);
			}
		} else if (sort != null) {
			query.sortAsc(sort);
		} else {
			query.sortAsc(Schemas.CAPTION).sortAsc(Schemas.CODE).sortAsc(Schemas.TITLE);
		}

		if (hasSecurity) {
			if (writeAccessRequired) {
				query.filteredWithUserWrite(user);
			} else {
				query.filteredWithUser(user);
			}
		}

		if (metadataFilter != null) {
			query.setReturnedMetadatas(metadataFilter);
		}

		if (locale != null) {
			query.setLanguage(locale);
		}

		return query;
	}

	private LogicalSearchCondition addIgnoreIdsToCondition(LogicalSearchCondition condition) {
		if (idsToIgnore != null && idsToIgnore.size() > 0) {
			return condition.andWhere(Schemas.IDENTIFIER).isNotIn(idsToIgnore);
		} else {
			return condition;
		}
	}
}
