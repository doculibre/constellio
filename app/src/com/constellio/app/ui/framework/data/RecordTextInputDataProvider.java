package com.constellio.app.ui.framework.data;

import com.constellio.app.api.extensions.params.RecordTextInputDataProviderSortMetadatasParam;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.ConverterWithCache;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatchingInMetadatas;

public class RecordTextInputDataProvider extends TextInputDataProvider<String> {

	private transient int lastStartIndex;
	private transient String lastQuery;
	private transient SPEQueryResponse response;

	private transient AppLayerFactory appLayerFactory;
	private transient ModelLayerFactory modelLayerFactory;
	protected SessionContext sessionContext;
	protected String schemaTypeCode;
	protected String schemaCode;
	protected boolean security;
	protected boolean onlyLinkables = false;
	protected boolean writeAccess;
	protected boolean includeDeactivated;
	protected boolean includeLogicallyDeleted;
	protected ConverterWithCache<String, String> converterWithCache;

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, boolean writeAccess) {
		this(constellioFactories, sessionContext, schemaTypeCode, null, writeAccess, true);
	}

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, boolean writeAccess, boolean includeDeactivated) {
		this(constellioFactories, sessionContext, schemaTypeCode, null, writeAccess, includeDeactivated);
	}

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, String schemaCode, boolean writeAccess) {
		this(constellioFactories, sessionContext, schemaTypeCode, schemaCode, writeAccess, true);
	}

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, String schemaCode, boolean writeAccess,
									   boolean includeDeactivated) {
		this(constellioFactories, sessionContext, schemaTypeCode, schemaCode, writeAccess, includeDeactivated, false, false);
	}

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, String schemaCode, boolean writeAccess,
									   boolean includeDeactivated, boolean onlyLinkables,
									   boolean includeLogicallyDeleted) {
		this.writeAccess = writeAccess;
		this.sessionContext = sessionContext;
		this.schemaTypeCode = schemaTypeCode;
		this.schemaCode = schemaCode;
		this.appLayerFactory = constellioFactories.getAppLayerFactory();
		this.modelLayerFactory = constellioFactories.getModelLayerFactory();
		this.security = determineIfSecurity();
		this.includeDeactivated = includeDeactivated;
		this.onlyLinkables = onlyLinkables;
		this.includeLogicallyDeleted = includeLogicallyDeleted;
	}

	public RecordTextInputDataProvider setConverterWithCache(
			ConverterWithCache<String, String> converterWithCache) {
		this.converterWithCache = converterWithCache;
		return this;
	}

	private boolean determineIfSecurity() {
		boolean security = false;

		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(getCurrentCollection());
		List<MetadataSchemaType> typesByCode = new ArrayList<MetadataSchemaType>();
		if (schemaTypeCode != null) {
			MetadataSchemaType type = types.getSchemaType(schemaTypeCode);
			typesByCode.add(type);
		} else if (schemaCode != null) {
			String schemaTypeCodeFromSchema = SchemaUtils.getSchemaTypeCode(schemaCode);
			MetadataSchemaType type = types.getSchemaType(schemaTypeCodeFromSchema);
			typesByCode.add(type);
		}
		for (MetadataSchemaType type : typesByCode) {
			if (type.hasSecurity() || modelLayerFactory.getTaxonomiesManager().isTypeInPrincipalTaxonomy(type)) {
				security = true;
				break;
			}
		}
		return security;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		appLayerFactory = constellioFactories.getAppLayerFactory();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
	}

	@Override
	public List<String> getData(String text, int startIndex, int count) {
		User user = getCurrentUser();
		if (lastQuery == null || !lastQuery.equals(text) || lastStartIndex != startIndex) {
			lastQuery = text;
			lastStartIndex = startIndex;
			response = searchAutocompleteField(user, text, startIndex, count);
		}
		return toRecordIds(response.getRecords());
	}

	@Override
	public int size(String text) {
		User user = getCurrentUser();
		if (lastQuery == null || !lastQuery.equals(text)) {
			lastQuery = text;
			lastStartIndex = -1;
			response = searchAutocompleteField(user, text, 0, 1);
		}
		return (int) response.getNumFound();
	}

	protected List<String> toRecordIds(List<Record> matches) {
		List<String> recordIds = new ArrayList<>();
		for (Record match : matches) {
			if (converterWithCache != null) {
				String caption = match.get(Schemas.CAPTION);
				if (caption != null) {
					converterWithCache.preload(match.getId(), caption);
				}
			}

			recordIds.add(match.getId());
		}
		return recordIds;
	}

	public SPEQueryResponse searchAutocompleteField(User user, String text, int startIndex, int count) {
		LogicalSearchQuery query = getQuery(user, text, startIndex, count);
		return modelLayerFactory.newSearchServices().query(query.setName("Autocomplete"));
	}

	public LogicalSearchQuery getQuery(User user, String text, int startIndex, int count) {
		LogicalSearchCondition condition;

		Metadata sort = null;
		if (schemaTypeCode != null) {
			MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(getCurrentCollection()).getSchemaType(schemaTypeCode);
			List<Metadata> extraMetadatas = type.getDefaultSchema().getMetadatas().onlySearchable()
					.onlySchemaAutocomplete().excludingValueTypes(STRUCTURE);
			if (StringUtils.isNotBlank(text)) {
				condition = from(type).where(autocompleteFieldMatchingInMetadatas(text, extraMetadatas));
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
					.getSchemaTypes(getCurrentCollection()).getSchema(schemaCode);
			List<Metadata> extraMetadatas = schema.getMetadatas().onlySearchable().onlySchemaAutocomplete();
			if (StringUtils.isNotBlank(text)) {
				condition = from(schema).where(autocompleteFieldMatchingInMetadatas(text, extraMetadatas));
			} else {
				condition = from(schema).returnAll();
			}
		}
		if (onlyLinkables) {
			condition = condition.andWhere(Schemas.LINKABLE).isTrueOrNull();
		}

		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.setPreferAnalyzedFields(true)
				.setStartRow(startIndex)
				.setNumberOfRows(count)
				.setName("Autocomplete query for input '" + text + "'");

		//		boolean isDDV = schemaTypeCode != null? schemaTypeCode.startsWith("ddv"):schemaCode.startsWith("ddv");
		if (!includeLogicallyDeleted) {
			query.filteredByStatus(StatusFilter.ACTIVES);
		}

		String collection = sessionContext.getCurrentCollection();
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

		if (security) {
			if (writeAccess) {
				query.filteredWithUserWrite(user);
			} else {
				query.filteredWithUser(user);
			}
		}
		query.setLanguage(sessionContext.getCurrentLocale());
		return query;
	}

	protected String getCurrentCollection() {
		return sessionContext.getCurrentCollection();
	}

	public User getCurrentUser() {
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();
		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	@Override
	public void setOnlyLinkables(boolean onlyLinkables) {
		this.onlyLinkables = onlyLinkables;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}
}
