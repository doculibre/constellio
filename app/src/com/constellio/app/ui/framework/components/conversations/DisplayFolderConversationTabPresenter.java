package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.ConversationSearchService;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DisplayFolderConversationTabPresenter {

	private final String collection;
	private final SessionContext sessionContext;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private final EventObservable<SearchResultsAvailableArgs> searchResultAvailableObservable;
	private final EventObservable<SearchClearedArgs> searchClearedObservable;

	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private BasePresenterUtils presenterUtils;
	private ModelLayerFactory modelLayerFactory;
	private AppLayerFactory appLayerFactory;
	private String conversationId;

	public DisplayFolderConversationTabPresenter(String collection, SessionContext sessionContext,
												 String conversationId,
												 ConversationFacetsHandler conversationFacetsHandler) {
		this.collection = collection;
		this.sessionContext = sessionContext;
		this.conversationId = conversationId;
		this.conversationFacetsHandler = conversationFacetsHandler;

		this.searchResultAvailableObservable = new EventObservable<>();
		this.searchClearedObservable = new EventObservable<>();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
		appLayerFactory = constellioFactories.getAppLayerFactory();

		presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);
	}

	public void clearSearch() {
		searchClearedObservable.fire(new SearchClearedArgs(this));
	}

	public List<String> getAutocompleteSuggestions(String text) {
		return new ArrayList<>();
	}

	public int getAutocompleteBufferSize() {
		return 0;
	}

	public void searchInConversation(String searchValue) {
		if (!StringUtils.isBlank(searchValue)) {
			ConversationSearchService conversationMessageSearchService = new ConversationSearchService(collection, sessionContext, modelLayerFactory, appLayerFactory, conversationFacetsHandler);

			searchResultAvailableObservable.fire(new SearchResultsAvailableArgs(this, conversationMessageSearchService.searchInConversation(conversationId, searchValue)));
		} else {
			clearSearch();
		}
	}

	public LogicalSearchQuery buildQueryForFacetsSelection(String searchValue) {
		ConversationSearchService conversationMessageSearchService = new ConversationSearchService(collection, sessionContext, modelLayerFactory, appLayerFactory, conversationFacetsHandler);
		return conversationMessageSearchService.buildQueryForFacetsSelection(conversationId, searchValue);
	}

	public void addNewSearchResultAvailableListener(EventListener<SearchResultsAvailableArgs> listener) {
		searchResultAvailableObservable.addListener(listener);
	}

	public void removeNewSearchResultAvailableListener(EventListener<SearchResultsAvailableArgs> listener) {
		searchResultAvailableObservable.removeListener(listener);
	}

	public void addSearchClearedListener(EventListener<SearchClearedArgs> listener) {
		searchClearedObservable.addListener(listener);
	}

	public void removeSearchClearedListener(EventListener<SearchClearedArgs> listener) {
		searchClearedObservable.removeListener(listener);
	}

	static class SearchResultsAvailableArgs extends EventArgs<DisplayFolderConversationTabPresenter> {
		private final RecordVODataProvider dataProvider;

		public SearchResultsAvailableArgs(DisplayFolderConversationTabPresenter sender,
										  RecordVODataProvider dataProvider) {
			super(sender);
			this.dataProvider = dataProvider;
		}

		public RecordVODataProvider getDataProvider() {
			return dataProvider;
		}
	}

	static class SearchClearedArgs extends EventArgs<DisplayFolderConversationTabPresenter> {
		public SearchClearedArgs(DisplayFolderConversationTabPresenter sender) {
			super(sender);
		}
	}
}
