package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Component;


public class ConversationMessageSearchResultDisplay extends SearchResultDisplay {

	public ConversationMessageSearchResultDisplay(SearchResultVO searchResultVO,
												  MetadataDisplayFactory componentFactory,
												  AppLayerFactory appLayerFactory, String query, boolean noLinks) {
		super(searchResultVO, componentFactory, appLayerFactory, query, noLinks);


	}

	@Override
	protected Component newTitleLink(SearchResultVO searchResultVO) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		ConversationFacetsHandler conversationFacetsHandler = new ConversationFacetsHandler(searchResultVO.getRecordVO().getRecord().getCollection(), sessionContext, appLayerFactory.getModelLayerFactory());
		ConversationMessageFactory conversationMessageFactory = new ConversationMessageFactory(sessionContext, conversationFacetsHandler);

		return conversationMessageFactory.build(searchResultVO.getRecordVO().getRecord(), appLayerFactory, VIEW_MODE.SEARCH);
	}
}
