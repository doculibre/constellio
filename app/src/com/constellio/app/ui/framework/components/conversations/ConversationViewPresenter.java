package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.app.ui.pages.search.SearchPresenterService;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Collections;
import java.util.List;

public class ConversationViewPresenter {

	private final String collection;
	private BasePresenterUtils presenterUtils;
	transient SearchPresenterService service;
	private User user;
	private SessionContext sessionContext;

	private boolean applyButtonFacetEnabled;
	private final ConversationMessageList conversationMessageList;
	private ModelLayerFactory modelLayerFactory;

	ConversationFacetsHandler conversationFacetsHandler;

	public ConversationViewPresenter(String collection, ConversationMessageList conversationMessageList,
									 ConversationFacetsHandler conversationFacetsHandler) {
		this.collection = collection;
		this.conversationMessageList = conversationMessageList;
		this.sessionContext = conversationMessageList.getSessionContext();

		this.conversationFacetsHandler = conversationFacetsHandler;

		initTransientObjects(collection);
	}

	private void initTransientObjects(String collection) {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		modelLayerFactory = constellioFactories.getModelLayerFactory();

		presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);

		user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(sessionContext.getCurrentUser().getUsername(), collection);
		applyButtonFacetEnabled = user.isApplyFacetsEnabled();
		List<MetadataSchemaType> types = Collections.singletonList(presenterUtils.schemaType(Conversation.SCHEMA_TYPE));
		service = new SearchPresenterService(collection, user, modelLayerFactory, types);

	}

	public boolean isFacetApplyButtonEnabled() {
		return applyButtonFacetEnabled;
	}

	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		conversationFacetsHandler.selectSortCriterion(sortCriterion, sortOrder);
	}

	public void facetValueSelected(String facetId, String facetValue) {
		conversationFacetsHandler.selectFacetValue(facetId, facetValue);
	}

	public void facetValuesChanged(KeySetMap<String, String> facets) {
		conversationFacetsHandler.changeFacetValues(facets);
	}

	public void facetValueDeselected(String facetId, String facetValue) {
		conversationFacetsHandler.deselectFacetValue(facetId, facetValue);
	}

	public void facetDeselected(String facetId) {
		conversationFacetsHandler.deselectFacet(facetId);
	}

	public void facetOpened(String facetId) {
		conversationFacetsHandler.openFacet(facetId);
	}

	public void facetClosed(String facetId) {
		conversationFacetsHandler.closeFacet(facetId);
	}

	public List<FacetVO> getFacets(LogicalSearchQuery query) {

		if (query == null) {
			query = conversationMessageList.getDataProvider().buildQueryForFacetsSelection(null);
		}

		return conversationFacetsHandler.getFacetsForQuery(query);
	}

	public void refresh() {
		conversationMessageList.getDataProvider().loadOlderMessagesThanThisMessage(null);
	}

	public KeySetMap<String, String> getFacetSelections() {
		return conversationFacetsHandler.getFacetSelections();
	}

	public String getSortCriterion() {
		return conversationFacetsHandler.getSortCriterion();
	}

	public SortOrder getSortOrder() {
		return conversationFacetsHandler.getSortOrder();
	}

	public List<MetadataVO> getMetadataAllowedInSort() {
		return conversationFacetsHandler.getMetadataAllowedInSort();
	}

	public String getSortCriterionValueAmong(List<MetadataVO> sortableMetadata) {
		return conversationFacetsHandler.getSortCriterionValueAmong(sortableMetadata);
	}
}
