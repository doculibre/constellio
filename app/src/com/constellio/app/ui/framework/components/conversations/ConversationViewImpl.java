package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.ConversationMessageListArgs;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.WhatToLoadWhenNoTargetedMessage;
import com.constellio.app.ui.framework.components.search.FacetsPanel;
import com.constellio.app.ui.framework.components.search.FacetsSliderPanel;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ConversationViewImpl extends Panel implements ConversationView {
	private final String collection;
	private final SessionContext sessionContext;
	private final String conversationId;
	private final String targetedMessageId;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private Supplier<Boolean> hasOlderMessagesToLoad;
	private Runnable loadOlderMessages;

	private Supplier<Boolean> hasNewerMessagesToLoad;
	private Runnable loadNewerMessages;


	private final JavaScript jsEngine;

	private String scrollableQuerySelector;

	private boolean facetsPanelVisible;

	private FacetsPanel facetsPanel;
	private FacetsSliderPanel facetsSliderPanel;

	private ConversationViewPresenter presenter;


	public ConversationViewImpl(String collection, SessionContext sessionContext,
								ConversationFacetsHandler conversationFacetsHandler, String conversationId) {
		this(collection, sessionContext, conversationFacetsHandler, conversationId, null);
	}

	public ConversationViewImpl(String collection, SessionContext sessionContext,
								ConversationFacetsHandler conversationFacetsHandler, String conversationId,
								String targetedMessageId) {
		this.collection = collection;
		this.conversationId = conversationId;
		this.sessionContext = sessionContext;
		this.targetedMessageId = targetedMessageId;
		this.conversationFacetsHandler = conversationFacetsHandler;

		addStyleName(ValoTheme.PANEL_BORDERLESS);
		addStyleName("v-scrollable");
		addStyleName("conversation");

		if (ConstellioUI.getCurrent().isNested()) {
			addStyleName("conversation-nested");
		}

		jsEngine = JavaScript.getCurrent();

		facetsPanelVisible = false;
	}

	@Override
	public void attach() {
		super.attach();

		buildComponent();
	}

	@Override
	public void detach() {
		super.detach();

		unregisterLoadingOnScrollForConversationMessageList();
	}

	public void buildComponent() {
		setSizeFull();

		setId("conversation" + conversationId);

		scrollableQuerySelector = "#" + getId() + " .v-panel-content";
		if (ConstellioUI.getCurrent().isNested()) {
			scrollableQuerySelector = "#content-footer-wrapper";
		}


		ConversationMessageListImpl conversationMessageList = new ConversationMessageListImpl(new ConversationMessageListArgs(
				collection,
				sessionContext,
				conversationFacetsHandler,
				conversationId) {

			@Override
			public String getTargetedMessageId() {
				return targetedMessageId;
			}

			@Override
			public String getParentMessageId() {
				return ConversationViewImpl.this.getParentMessageId();
			}

			@Override
			public WhatToLoadWhenNoTargetedMessage getWhatToLoadWhenNoTargetedMessage() {
				return WhatToLoadWhenNoTargetedMessage.LOAD_NEWEST_MESSAGE;
			}

			@Override
			public String getQuerySelectorForScrollablePanel() {
				return scrollableQuerySelector;
			}
		}) {
			@Override
			public Component buildTopControls(Supplier<Boolean> hasOlderMessagesToLoad, Runnable loadOlderMessages) {
				ConversationViewImpl.this.hasOlderMessagesToLoad = hasOlderMessagesToLoad;
				ConversationViewImpl.this.loadOlderMessages = loadOlderMessages;

				Component topControls = super.buildTopControls(hasOlderMessagesToLoad, loadOlderMessages);
				topControls.setVisible(false);
				return topControls;
			}

			@Override
			public Component buildBottomControls(Supplier<Boolean> hasNewerMessagesToLoad, Runnable loadNewerMessages) {
				ConversationViewImpl.this.hasNewerMessagesToLoad = hasNewerMessagesToLoad;
				ConversationViewImpl.this.loadNewerMessages = loadNewerMessages;

				Component bottomControls = super.buildTopControls(hasNewerMessagesToLoad, loadNewerMessages);
				bottomControls.setVisible(false);
				return bottomControls;
			}
		};
		conversationMessageList.addListRefreshedListener(event -> {

			if (event.isNewContext()) {
				executeIfItemIsVisibleOnScreen(conversationMessageList.getFirstItemLoadedOnClient(), () -> {
					if (hasOlderMessagesToLoad.get()) {
						loadOlderMessages.run();
					}
				});

				executeIfItemIsVisibleOnScreen(conversationMessageList.getLastItemLoadedOnClient(), () -> {
					if (hasNewerMessagesToLoad.get()) {
						loadNewerMessages.run();
					}
				});
			}
		});

		presenter = new ConversationViewPresenter(collection, conversationMessageList, conversationFacetsHandler);

		getFacetsPanel().setVisible(isFacetsPanelVisible());

		registerLoadingOnScrollForConversationMessageList(conversationMessageList);

		setContent(conversationMessageList);
	}

	private void buildFacetsSliderPanel() {
		facetsPanel = new FacetsPanel(presenter.isFacetApplyButtonEnabled()) {
			@Override
			protected void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
				presenter.sortCriterionSelected(sortCriterion, sortOrder);
			}

			@Override
			protected void facetValueSelected(String facetId, String value) {
				presenter.facetValueSelected(facetId, value);
			}

			@Override
			protected void facetValuesChanged(KeySetMap<String, String> facets) {
				presenter.facetValuesChanged(facets);
			}

			@Override
			protected void facetValueDeselected(String facetId, String value) {
				presenter.facetValueDeselected(facetId, value);
			}

			@Override
			protected void facetOpened(String id) {
				presenter.facetOpened(id);
			}

			@Override
			protected void facetDeselected(String id) {
				presenter.facetDeselected(id);
			}

			@Override
			protected void facetClosed(String id) {
				presenter.facetClosed(id);
			}
		};

		facetsSliderPanel = new FacetsSliderPanel(facetsPanel);
	}

	public String getParentMessageId() {
		return null;
	}

	public void refreshFacets() {
		refreshFacets(null);
	}

	public void refreshFacets(LogicalSearchQuery refreshWithThisQuery) {
		List<FacetVO> facets = presenter.getFacets(refreshWithThisQuery);
		KeySetMap<String, String> facetSelections = presenter.getFacetSelections();
		List<MetadataVO> sortableMetadata = presenter.getMetadataAllowedInSort();
		String sortCriterionValue = presenter.getSortCriterionValueAmong(sortableMetadata);
		SortOrder sortOrder = presenter.getSortOrder();

		facetsPanel.setSortComponentVisible(false);
		facetsPanel.refresh(facets, facetSelections, sortableMetadata, sortCriterionValue, sortOrder);
	}

	private void registerLoadingOnScrollForConversationMessageList(
			ConversationMessageListImpl conversationMessageList) {
		String functionName = "scrollListenerFor" + getId() + UUID.randomUUID().toString().replace("-", "_");

		jsEngine.addFunction(functionName, args -> {
			if (args.length() > 0) {

				boolean isScrollingUp = args.getBoolean(0);

				if (isScrollingUp) {
					executeIfItemIsVisibleOnScreen(conversationMessageList.getFirstItemLoadedOnClient(), () -> {
						if (hasOlderMessagesToLoad.get()) {
							loadOlderMessages.run();
						}
					});
				} else {
					executeIfItemIsVisibleOnScreen(conversationMessageList.getLastItemLoadedOnClient(), () -> {
						if (hasNewerMessagesToLoad.get()) {
							loadNewerMessages.run();
						}
					});
				}
			}
		});

		jsEngine.execute("registerScrollCallback(" + functionName + ", \"" + scrollableQuerySelector + "\");");
	}

	private void unregisterLoadingOnScrollForConversationMessageList() {
		String functionName = "scrollListenerFor" + getId();

		jsEngine.execute("unregisterScrollCallback(" + functionName + ", \"" + scrollableQuerySelector + "\");");
		jsEngine.removeFunction(functionName);
	}

	private void executeIfItemIsVisibleOnScreen(Component itemThatMustBeVisible, Runnable whatToExecute) {
		String functionName = itemThatMustBeVisible.getId() + "isVisible" + UUID.randomUUID().toString().replace("-", "_");
		jsEngine.addFunction(functionName, args -> {

			boolean topIsVisible = args.getBoolean(0);
			boolean bottomIsVisible = args.getBoolean(2);
			boolean isVisible = topIsVisible || bottomIsVisible;

			if (isVisible && whatToExecute != null) {
				whatToExecute.run();
			}

			jsEngine.removeFunction(functionName);
		});

		jsEngine.execute("elementIsVisibleOnScreen(\"" + itemThatMustBeVisible.getId() + "\", " + functionName + ", \"" + scrollableQuerySelector + "\")");
	}

	public FacetsSliderPanel getFacetsPanel() {
		if (facetsSliderPanel == null) {
			buildFacetsSliderPanel();
		}

		return facetsSliderPanel;
	}

	public void setFacetsPanelVisible(boolean visible) {
		this.facetsPanelVisible = visible;
		getFacetsPanel().setVisible(isFacetsPanelVisible());
	}

	public boolean isFacetsPanelVisible() {
		return facetsPanelVisible;
	}

	public void refresh() {
		presenter.refresh();
	}
}
