package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.ConverterWithCache;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordTextInputDataProvider extends TextInputDataProvider<String> {

	private transient int lastStartIndex;
	private transient String lastQuery;
	private transient SPEQueryResponse response;
	private transient int lastCount;

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
	protected VisibilityStatusFilter visibilityStatus = VisibilityStatusFilter.ALL;
	protected ConverterWithCache<String, String> converterWithCache;
	protected List<String> idsToIgnore;

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
		this(constellioFactories, sessionContext, schemaTypeCode, schemaCode, writeAccess, includeDeactivated, onlyLinkables, includeLogicallyDeleted, VisibilityStatusFilter.ALL);
	}

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
									   String schemaTypeCode, String schemaCode, boolean writeAccess,
									   boolean includeDeactivated, boolean onlyLinkables,
									   boolean includeLogicallyDeleted, VisibilityStatusFilter visibilityStatus  ) {
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
		this.idsToIgnore = new ArrayList<>();
		this.visibilityStatus = visibilityStatus;
	}


	public RecordTextInputDataProvider addIdToToIgnore(List<String> idToIgnore) {
		if (idToIgnore != null) {
			this.idsToIgnore.addAll(idToIgnore);
		}

		return this;
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
		if (lastQuery == null || !lastQuery.equals(text) || lastStartIndex != startIndex || lastCount != count) {
			lastQuery = text;
			lastCount = count;
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
		return AutocompleteQuery.builder()
				.appLayerFactory(appLayerFactory)
				.schemaTypeCode(schemaTypeCode).schemaCode(schemaCode)
				.hasSecurity(security)
				.expression(text)
				.user(user).writeAccessRequired(writeAccess)
				.startRow(startIndex).rowCount(count)
				.locale(sessionContext.getCurrentLocale())
				.onlyLinkables(onlyLinkables)
				.includeDeactivated(includeDeactivated)
				.includeLogicallyDeleted(includeLogicallyDeleted)
				.visibilityStatus(visibilityStatus)
				.idsToIgnore(idsToIgnore)
				.build().searchQuery();
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
