package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.PlaceHolder;
import com.constellio.app.ui.framework.components.conversations.DisplayFolderConversationTabPresenter.SearchClearedArgs;
import com.constellio.app.ui.framework.components.conversations.DisplayFolderConversationTabPresenter.SearchResultsAvailableArgs;
import com.constellio.app.ui.framework.components.fields.autocomplete.StringAutocompleteField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.search.FacetsSliderPanel;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.sliderpanel.client.SliderPanelListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayFolderConversationTab {
	private DisplayFolderViewImpl displayFolderView;
	private final Folder folder;
	private final AppLayerFactory appLayerFactory;

	private TabSheet tabSheet;
	private Tab conversationTab;

	private Component componentInTab;

	private VerticalLayout conversationView;
	private ConversationViewImpl conversation;
	private Component searchResult;

	private StringAutocompleteField<String> searchField;

	private SelectedTabChangeListener selectedTabChangeListener;

	private DisplayFolderConversationTabPresenter presenter;

	public DisplayFolderConversationTab(Folder folder, AppLayerFactory appLayerFactory) {
		this.folder = folder;
		this.appLayerFactory = appLayerFactory;
	}

	public void addTabToThisView(DisplayFolderViewImpl displayFolderView) {
		this.displayFolderView = displayFolderView;

		componentInTab = new PlaceHolder();

		tabSheet = displayFolderView.getTabSheet();
		tabSheet.addTab(componentInTab, $("Conversation.displayFolderTab.caption"));
		conversationTab = tabSheet.getTab(componentInTab);
		conversationTab.setIcon(FontAwesome.COMMENT);

		selectedTabChangeListener = this::selectedTabChangeListenerWhenItsPlaceholder;
		tabSheet.addSelectedTabChangeListener(this::selectedTabChangeListener);
	}

	private void selectedTabChangeListener(SelectedTabChangeEvent event) {
		selectedTabChangeListener.selectedTabChange(event);
	}

	private void selectedTabChangeListenerWhenItsPlaceholder(SelectedTabChangeEvent event) {
		if (isConversationTabSelected(event)) {
			TabSheet tabSheet = event.getTabSheet();

			ConversationFacetsHandler conversationFacetsHandler = new ConversationFacetsHandler(folder.getCollection(), displayFolderView.getSessionContext(), appLayerFactory.getModelLayerFactory());
			conversationView = new VerticalLayout();

			conversation = new ConversationViewImpl(folder.getCollection(), displayFolderView.getSessionContext(), conversationFacetsHandler, folder.getConversation());

			conversationView.addComponents(conversation);

			tabSheet.replaceComponent(tabSheet.getSelectedTab(), conversationView);
			conversationTab = tabSheet.getTab(conversationView);

			addFacetsAndSearchToTabContent(conversationFacetsHandler);

			selectedTabChangeListener = this::selectedTabChangeListenerWhenItsConversation;

			conversationTabSelected();
		}
	}

	private void addFacetsAndSearchToTabContent(ConversationFacetsHandler conversationFacetsHandler) {

		presenter = new DisplayFolderConversationTabPresenter(folder.getCollection(), displayFolderView.getSessionContext(), folder.getConversation(), conversationFacetsHandler);
		presenter.addNewSearchResultAvailableListener(this::searchResultAvailable);
		presenter.addSearchClearedListener(this::searchCleared);

		conversationView.addComponent(buildSearchLayout(), 0);

		if (!displayFolderView.isNestedView()) {
			FacetsSliderPanel facetsSliderPanel = conversation.getFacetsPanel();
			final AtomicBoolean facetsPanelLoaded = new AtomicBoolean(false);
			facetsSliderPanel.addListener((SliderPanelListener) expand -> {
				if (expand && !facetsPanelLoaded.get()) {
					facetsPanelLoaded.set(true);
					loadFacets();
				}
			});

			displayFolderView.addFacetsPanel(facetsSliderPanel);

			conversationFacetsHandler.addFacetChangedListener(eventArgs -> refreshFacets());
		}
	}

	private void loadFacets() {
		if (conversationView.getComponentIndex(conversation) > 0) {
			conversation.refreshFacets();
		}
		if (conversationView.getComponentIndex(searchResult) > 0) {
			presenter.searchInConversation(searchField.getValue());
		}
	}

	private void refreshFacets() {
		if (conversationView.getComponentIndex(conversation) > 0) {
			conversation.refresh();
			conversation.refreshFacets();
		}
		if (conversationView.getComponentIndex(searchResult) > 0) {
			presenter.searchInConversation(searchField.getValue());
		}
	}

	public void selectedTabChangeListenerWhenItsConversation(SelectedTabChangeEvent event) {
		if (isConversationTabSelected(event)) {
			conversationTabSelected();
		} else {
			conversation.setFacetsPanelVisible(false);
		}
	}

	private boolean isConversationTabSelected(SelectedTabChangeEvent event) {
		TabSheet tabSheet = event.getTabSheet();
		Component selectedComponent = tabSheet.getSelectedTab();
		Tab selectedTab = tabSheet.getTab(selectedComponent);

		return selectedTab != null && selectedTab.equals(conversationTab);
	}

	private void conversationTabSelected() {
		if (displayFolderView != null) {
			displayFolderView.setFacetsPanelVisible(false);
		}

		if (conversation != null) {
			conversation.setFacetsPanelVisible(true);
		}
	}

	public void searchResultAvailable(SearchResultsAvailableArgs args) {
		int conversationIndex = conversationView.getComponentIndex(conversation);
		boolean conversationisShown = conversationIndex >= 0;

		RecordVODataProvider dataProvider = args.getDataProvider();


		if (conversationisShown) {
			conversationView.removeComponent(conversation);
		}

		RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(dataProvider);
		ViewableRecordVOTablePanel searchResultPanel = new ViewableRecordVOTablePanel(recordVOContainer) {
			@Override
			protected boolean isShowThumbnailCol() {
				return true;
			}


			@Override
			protected boolean isSelectColumn() {
				return !displayFolderView.isNestedView();
			}

			@Override
			public boolean isNested() {
				return displayFolderView.isNestedView();
			}
		};

		if (conversationView.getComponentIndex(conversation) > 0) {
			conversationView.replaceComponent(conversation, searchResultPanel);
		}
		if (conversationView.getComponentIndex(searchResult) > 0) {
			conversationView.replaceComponent(searchResult, searchResultPanel);
		} else {
			conversationView.addComponents(searchResultPanel);
		}

		searchResult = searchResultPanel;
		conversation.refreshFacets(presenter.buildQueryForFacetsSelection(searchField.getValue()));
	}

	public void searchCleared(SearchClearedArgs args) {
		if (conversationView.getComponentIndex(searchResult) > 0) {
			conversationView.replaceComponent(searchResult, conversation);
			conversation.refreshFacets();
		}
	}

	public Component buildSearchLayout() {
		final VerticalLayout searchLayout = new VerticalLayout();
		searchLayout.addStyleName("folder-search-layout");
		searchLayout.setSpacing(true);
		searchLayout.setWidth("50%");
		searchLayout.setVisible(false);

		searchField = new StringAutocompleteField<>(new StringAutocompleteField.AutocompleteSuggestionsProvider<String>() {
			@Override
			public List<String> suggest(String text) {
				return presenter.getAutocompleteSuggestions(text);
			}

			@Override
			public Class<String> getModelType() {
				return String.class;
			}

			@Override
			public int getBufferSize() {
				return presenter.getAutocompleteBufferSize();
			}
		});
		searchField.setWidth("100%");
		searchField.addStyleName("folder-search-field");

		final SearchButton searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				String value = searchField.getValue();
				presenter.searchInConversation(value);
			}
		};
		searchButton.addStyleName("folder-search-button");
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.setIconOnly(true);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				String value = searchField.getValue();
				presenter.searchInConversation(value);
			}
		};
		onEnterHandler.installOn(searchField);

		BaseButton clearSearchButton = new LinkButton($("DisplayFolderView.clearSearch")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.clearSearch();
				searchField.setValue("");
			}
		};
		clearSearchButton.addStyleName("folder-search-clear");

		BaseButton searchInFolderButton = new LinkButton($("Conversation.displayFolderTab.showSearchInFolder")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (searchLayout.isVisible()) {
					setCaption($("Conversation.displayFolderTab.showSearchInFolder"));
				} else {
					setCaption($("Conversation.displayFolderTab.hideSearchInFolder"));
				}
				searchLayout.setVisible(!searchLayout.isVisible());
				searchField.focus();
			}
		};
		searchInFolderButton.addStyleName("search-in-folder-button");

		I18NHorizontalLayout searchFieldAndButtonLayout = new I18NHorizontalLayout(searchField, searchButton);
		searchFieldAndButtonLayout.addStyleName("folder-search-field-and-button-layout");
		searchFieldAndButtonLayout.setWidth("100%");
		searchFieldAndButtonLayout.setExpandRatio(searchField, 1);

		I18NHorizontalLayout extraFieldsSearchLayout = new I18NHorizontalLayout(clearSearchButton);
		extraFieldsSearchLayout.addStyleName("folder-search-extra-fields-layout");
		extraFieldsSearchLayout.setSpacing(true);

		searchLayout.addComponents(searchFieldAndButtonLayout, extraFieldsSearchLayout);

		VerticalLayout searchToggleAndFieldsLayout = new VerticalLayout();
		searchToggleAndFieldsLayout.addStyleName("search-folder-toggle-and-fields-layout");
		searchToggleAndFieldsLayout.addComponent(searchInFolderButton);
		searchToggleAndFieldsLayout.addComponent(searchLayout);

		return searchToggleAndFieldsLayout;
	}
}
