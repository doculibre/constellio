package com.constellio.app.ui.framework.data;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;

public class RecordTextInputDataProvider implements TextInputDataProvider<String> {

	private transient int lastStartIndex;
	private transient String lastQuery;
	private transient SPEQueryResponse response;

	private transient ModelLayerFactory modelLayerFactory;
	protected SessionContext sessionContext;
	protected String schemaTypeCode;
	protected boolean security;
	protected boolean onlyLinkables = false;
	protected boolean writeAccess;

	public RecordTextInputDataProvider(ConstellioFactories constellioFactories, SessionContext sessionContext,
			String schemaTypeCode, boolean writeAccess) {
		this.writeAccess = writeAccess;
		this.sessionContext = sessionContext;
		this.schemaTypeCode = schemaTypeCode;
		this.modelLayerFactory = constellioFactories.getModelLayerFactory();
		this.security = determineIfSecurity();
	}

	private boolean determineIfSecurity() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(getCurrentCollection());
		MetadataSchemaType type = types.getSchemaType(schemaTypeCode);
		return type.hasSecurity() || modelLayerFactory.getTaxonomiesManager().isTypeInPrincipalTaxonomy(type);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
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
			recordIds.add(match.getId());
		}
		return recordIds;
	}

	public SPEQueryResponse searchAutocompleteField(User user, String text, int startIndex, int count) {
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(getCurrentCollection()).getSchemaType(schemaTypeCode);

		LogicalSearchCondition condition;
		if (StringUtils.isNotBlank(text)) {
			condition = from(type).where(autocompleteFieldMatching(text));
		} else {
			condition = from(type).returnAll();
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

		return modelLayerFactory.newSearchServices().query(query);

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
