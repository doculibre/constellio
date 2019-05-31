package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BasePopupView;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.autocomplete.StringAutocompleteField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.SessionContext.SelectedRecordIdsChangeListener;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.Responsive;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class ConstellioHeaderImpl extends I18NHorizontalLayout implements ConstellioHeader, SelectedRecordIdsChangeListener {

	private static final String POPUP_ID = "header-popup";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME = "header-show-advanced-search-button-popup-hidden";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME = "header-show-advanced-search-button-popup-visible";

	private List<String> collections = new ArrayList<>();

	private final ConstellioHeaderPresenter presenter;

	private StringAutocompleteField<String> searchField;
	private WindowButton selectionButton;

	private BasePopupView popupView;

	private Button showAdvancedSearchButton;
	private ComboBox advancedSearchSchemaTypeField;
	private Component advancedSearchForm;
	private Button clearAdvancedSearchButton;
	private AdvancedSearchCriteriaComponent criteria;

	private Component selectionPanel;
	private Table selectionTable;
	private SelectionTableAdapter selectionTableAdapter;
	private VerticalLayout actionMenuLayout;
	private int selectionCount;

	private Boolean delayedSelectionButtonEnabled;
	private BaseView currentView;

	private MenuItem collectionSubMenu;
	private CollectionCodeToLabelConverter collectionCodeToLabelConverter = new CollectionCodeToLabelConverter();
	private HashMap<String, MenuItem> collectionButtons = new HashMap<>();
	private Locale locale;

	private List<NavigationItem> actionMenuItems;

	public ConstellioHeaderImpl() {
		presenter = new ConstellioHeaderPresenter(this);
		Resource resource = presenter.getUserLogoResource();
		Image logo = new Image("", resource != null ? resource : new ThemeResource("images/logo_eim_406x60.png"));
		logo.setHeight("30px");
		logo.setWidth("203px");
		logo.setAlternateText(("logo"));

		logo.addStyleName("header-logo");
		logo.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					presenter.logoClicked();
				}
			}
		});

		searchField = new StringAutocompleteField<String>(new StringAutocompleteField.AutocompleteSuggestionsProvider<String>() {
			@Override
			public List<String> suggest(String text) {
				return presenter.getAutocompleteSuggestions(text);
			}

			@Override
			public int getBufferSize() {
				return presenter.getAutocompleteBufferSize();
			}

			@Override
			public Class<String> getModelType() {
				return String.class;
			}
		});
		searchField.setMinChars(3);
		searchField.addStyleName("header-search");
		searchField.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				if (presenter.isValidAdvancedSearchCriterionPresent()) {
					presenter.advancedSearchFormButtonClicked();
				}
			}
		});

		showAdvancedSearchButton = new Button();
		showAdvancedSearchButton.addStyleName("header-show-advanced-search-button");
		showAdvancedSearchButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		showAdvancedSearchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.advancedSearchFormButtonClicked();
			}
		});
		showAdvancedSearchButton.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);

		Button searchButton = new SearchButton();
		searchButton.addStyleName("header-search-button");
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		});

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		};
		onEnterHandler.installOn(searchField);

		advancedSearchForm = buildAdvancedSearchUI();
		popupView = newPopupView(advancedSearchForm);

		MenuBar collectionMenu = buildCollectionMenu();
		MenuBar actionMenu = buildActionMenu();

		selectionButton = buildSelectionButton();
		setSelectionButtonIcon();
		selectionPanel = buildSelectionPanel();

		addComponents(logo, searchField, showAdvancedSearchButton, searchButton, collectionMenu, actionMenu, selectionButton,
				popupView);
		//		setComponentAlignment(headerMenu, Alignment.MIDDLE_RIGHT);
		setSizeFull();

		adjustSearchFieldContent();

		getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				currentView = (BaseView) event.getNewView();
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				if (!(event.getNewView() instanceof AdvancedSearchView || event.getNewView() instanceof SimpleSearchView)) {
					searchField.setValue(null);
					advancedSearchSchemaTypeField.setValue(null);
					criteria.clear();
					criteria.addEmptyCriterion().addEmptyCriterion();
					clearAdvancedSearchButton.setEnabled(false);
					adjustSearchFieldContent();
				}
			}
		});
	}

	private BasePopupView newPopupView(final Component component) {
		Responsive.makeResponsive(component);
		component.addStyleName("header-popup-content");
		component.setWidthUndefined();

		Panel wrapper = new Panel(component);
		wrapper.addStyleName("header-popup-content-wrapper");

		BasePopupView popupView = new BasePopupView("", wrapper);
		popupView.setId(POPUP_ID);
		popupView.addStyleName(POPUP_ID);
		popupView.setHideOnMouseOut(false);
		popupView.addPopupVisibilityListener(new PopupVisibilityListener() {
			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if (component == advancedSearchForm) {
					if (event.isPopupVisible()) {
						showAdvancedSearchButton.removeStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);
						showAdvancedSearchButton.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME);
					} else {
						showAdvancedSearchButton.removeStyleName(SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME);
						showAdvancedSearchButton.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);
					}
					adjustSearchFieldContent();
				}
				if (!event.isPopupVisible()) {
					presenter.popupClosed();
				}
			}
		});
		return popupView;
	}

	@Override
	public void setAdvancedSearchFormVisible(boolean visible) {
		if (visible) {
			if (popupView.getContent().getPopupComponent() != advancedSearchForm) {
				BasePopupView newPopupView = newPopupView(advancedSearchForm);
				replaceComponent(popupView, newPopupView);
				popupView = newPopupView;
			}
			popupView.setPopupVisible(true, false);
		} else {
			popupView.setPopupVisible(false, false);
		}
	}

	@Override
	public void setSelectionPanelVisible(boolean visible, boolean refresh) {
		if (visible) {
			if (refresh) {
				selectionPanel = buildSelectionPanel();
			}
			//			if (popupView.getContent().getPopupComponent() != selectionPanel) {
			//				BasePopupView newPopupView = newPopupView(selectionPanel);
			//				replaceComponent(popupView, newPopupView);
			//				popupView = newPopupView;
			//			}
			//			popupView.setPopupVisible(true, false);
			//		} else {
			//			popupView.setPopupVisible(false, false);
		}
		if (selectionPanel.isVisible() != visible) {
			selectionPanel.setVisible(visible);
		}
	}

	private void adjustSearchFieldContent() {
		if (popupView.isPopupVisible()) {
			searchField.setInputPrompt("");
		} else if (presenter.isValidAdvancedSearchCriterionPresent()) {
			searchField.setInputPrompt($("AdvancedSearchView.advancedCriteriaPrompt"));
		} else {
			searchField.setInputPrompt($("AdvancedSearchView.searchPrompt"));
		}
	}

	protected Component buildAdvancedSearchUI() {
		Button addCriterion = new Button($("add"));
		addCriterion.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);

		Component schemaTypeComponent = buildSchemaTypeComponent();
		I18NHorizontalLayout top = new I18NHorizontalLayout(schemaTypeComponent, addCriterion);
		top.setComponentAlignment(schemaTypeComponent, Alignment.BOTTOM_LEFT);
		top.setComponentAlignment(addCriterion, Alignment.BOTTOM_RIGHT);
		top.setWidth("100%");

		criteria = new AdvancedSearchCriteriaComponent(presenter);
		criteria.addEmptyCriterion();
		criteria.addEmptyCriterion();
		criteria.setWidth("100%");

		Button advancedSearch = new Button($("AdvancedSearchView.search"));
		advancedSearch.addStyleName(ValoTheme.BUTTON_PRIMARY);
		advancedSearch.addStyleName("advanced-search-button");
		advancedSearch.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		});

		clearAdvancedSearchButton = new Button($("AdvancedSearchView.clearAdvancedSearch"));
		clearAdvancedSearchButton.addStyleName(ValoTheme.BUTTON_LINK);
		clearAdvancedSearchButton.addStyleName("clear-advanced-search-button");
		clearAdvancedSearchButton.setEnabled(false);
		clearAdvancedSearchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				advancedSearchSchemaTypeField.setValue(null);
				criteria.clear();
				criteria.addEmptyCriterion().addEmptyCriterion();
				clearAdvancedSearchButton.setEnabled(false);
			}
		});

		Button savedSearches = new Button($("AdvancedSearchView.savedSearches"));
		savedSearches.addStyleName(ValoTheme.BUTTON_LINK);
		savedSearches.addStyleName("saved-searches-button");
		savedSearches.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.savedSearchPageRequested();
			}
		});

		I18NHorizontalLayout bottom = new I18NHorizontalLayout(advancedSearch, clearAdvancedSearchButton, savedSearches);
		bottom.addStyleName("header-popup-clear-and-search-buttons");
		bottom.setSpacing(true);

		VerticalLayout searchUI = new VerticalLayout(top, criteria, bottom);
		searchUI.setSpacing(true);
		return searchUI;
	}

	private Component buildSchemaTypeComponent() {
		Label label = new Label($("AdvancedSearchView.type"));

		advancedSearchSchemaTypeField = new BaseComboBox();
		for (MetadataSchemaTypeVO schemaType : presenter.getSchemaTypes()) {
			advancedSearchSchemaTypeField.addItem(schemaType.getCode());
			String itemCaption = schemaType.getLabel(Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage()));
			advancedSearchSchemaTypeField.setItemCaption(schemaType.getCode(), itemCaption);
		}
		advancedSearchSchemaTypeField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		advancedSearchSchemaTypeField.setNullSelectionAllowed(false);
		advancedSearchSchemaTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				selectAdvancedSearchSchemaType((String) advancedSearchSchemaTypeField.getValue());
			}
		});

		I18NHorizontalLayout layout = new I18NHorizontalLayout(label, advancedSearchSchemaTypeField);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	private WindowButton buildSelectionButton() {
		WindowConfiguration config = new WindowConfiguration(true, true, "80%", null);
		WindowButton selectionButton = new WindowButton($("ConstellioHeader.selection"),
				$("ConstellioHeader.selectionPanelTitle"), config) {
			@Override
			protected Component buildWindowContent() {
				return selectionPanel;
			}

			@Override
			protected boolean acceptWindowOpen(ClickEvent event) {
				presenter.selectionButtonClicked();
				return selectionPanel.isVisible();
			}
		};
		selectionButton.setBadgeVisible(true);
		selectionButton.setBadgeCount(selectionCount);
		selectionButton.addStyleName("header-selection-button");
		if (delayedSelectionButtonEnabled != null) {
			selectionButton.setEnabled(delayedSelectionButtonEnabled);
		}

		selectionButton.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				//Move to an extension if more cases are to be added
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				if (currentView != null && !(currentView instanceof SearchView)) {
					Navigator navigator = ConstellioUI.getCurrent().getNavigator();
					navigator.navigateTo(navigator.getState());
				}
			}
		});

		return selectionButton;
	}

	@Override
	public void attach() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		sessionContext.addSelectedRecordIdsChangeListener(this);
		super.attach();
	}

	@Override
	public void detach() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		sessionContext.removeSelectedRecordIdsChangeListener(this);
		super.detach();
	}

	@Override
	public void selectionCleared() {
		presenter.selectedRecordsCleared();
	}

	@Override
	public void recordIdRemoved(String recordId) {
		getSession().lock();
		presenter.selectedRecordIdRemoved(recordId);
		getSession().unlock();
	}

	@Override
	public void recordIdAdded(String recordId) {
		getSession().lock();
		presenter.selectedRecordIdAdded(recordId);
		getSession().unlock();
	}

	@SuppressWarnings({"unchecked"})
	private Component buildSelectionPanel() {
		final VerticalLayout selectionPanel = new VerticalLayout();
		selectionPanel.setSpacing(true);
		//		selectionPanel.setWidth("100%");
		selectionPanel.addStyleName("header-selection-panel");
		selectionPanel.addStyleName("no-scroll");

		I18NHorizontalLayout selectionLayout = new I18NHorizontalLayout();
		selectionLayout.setSpacing(true);
		selectionLayout.setWidth("100%");
		selectionLayout.addStyleName("header-selection-panel-layout");

		selectionTable = new BaseTable("selection-table");
		selectionTable.addContainerProperty("recordId", ReferenceDisplay.class, null);
		selectionTable.setWidth("100%");
		selectionTable.setColumnExpandRatio("recordId", 1);
		selectionTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

		List<String> selectedRecordIds = getSessionContext().getSelectedRecordIds();
		for (String selectedRecordId : selectedRecordIds) {
			ReferenceDisplay referenceDisplay = new ReferenceDisplay(selectedRecordId, false);
			Item item = selectionTable.addItem(selectedRecordId);
			item.getItemProperty("recordId").setValue(referenceDisplay);
		}

		Button clearSelectionButton = new BaseButton($("ConstellioHeader.clearSelection")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.clearSelectionButtonClicked();
			}
		};
		clearSelectionButton.addStyleName(ValoTheme.BUTTON_LINK);

		selectionTableAdapter = new SelectionTableAdapter(selectionTable) {
			@Override
			public void selectAll() {
				presenter.selectAllClicked();
			}

			@Override
			public void deselectAll() {
				presenter.deselectAllClicked();
			}

			@Override
			public boolean isAllItemsSelected() {
				return presenter.isAllItemsSelected();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return presenter.isAllItemsDeselected();
			}

			@Override
			public boolean isSelected(Object itemId) {
				String recordId = (String) itemId;
				return presenter.isSelected(recordId);
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				String recordId = (String) itemId;
				presenter.selectionChanged(recordId, selected);
				refreshButtons();
				adjustSelectAllButton(selected);
			}
		};
		Component component = selectionTableAdapter.getComponent(0);
		I18NHorizontalLayout topButtonsLayout = new I18NHorizontalLayout(component, clearSelectionButton);
		topButtonsLayout.setSpacing(true);
		selectionTableAdapter.addComponent(topButtonsLayout, 0);
		selectionTableAdapter.removeComponent(component);

		actionMenuLayout = new VerticalLayout();
		actionMenuLayout.addStyleName("header-selection-panel-actions");
		buildSelectionPanelButtons(actionMenuLayout);

		VerticalLayout selectionActionMenu = new VerticalLayout();
		selectionActionMenu.setWidth("210px");
		selectionActionMenu.setSpacing(true);
		selectionActionMenu.addComponent(actionMenuLayout);

		I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout();
		buttonsLayout.setSpacing(true);

		Button closeButton = new BaseButton($("ConstellioHeader.close")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closeWindow();
			}
		};

		buttonsLayout.addComponents(closeButton);
		buttonsLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		buttonsLayout.setSpacing(true);

		selectionPanel.addComponent(selectionLayout);
		selectionPanel.addComponent(buttonsLayout);
		selectionPanel.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

		selectionLayout.addComponent(selectionTableAdapter);
		selectionLayout.addComponent(selectionActionMenu);

		selectionLayout.setExpandRatio(selectionTableAdapter, 1);
		selectionLayout.setComponentAlignment(selectionActionMenu, Alignment.TOP_RIGHT);

		return selectionPanel;
	}

	private void buildSelectionPanelButtons(VerticalLayout actionMenuLayout) {
		WindowButton addToCartButton = buildAddToCartButton(actionMenuLayout);
		SelectionPanelExtension.setStyles(addToCartButton);
		actionMenuLayout.addComponents(addToCartButton);
		presenter.buildSelectionPanelActionButtons(actionMenuLayout);
	}

	private WindowButton buildAddToCartButton(final VerticalLayout actionMenuLayout) {
		WindowConfiguration configuration = new WindowConfiguration(true, true, "50%", "750px");
		WindowButton windowButton = new WindowButton($("ConstellioHeader.selection.actions.addToCart"),
				$("ConstellioHeader.selection.actions.addToCart"), configuration) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();

				HorizontalLayout newCartLayout = new HorizontalLayout();
				newCartLayout.setSpacing(true);
				newCartLayout.addComponent(new Label($("CartView.newCart")));
				final BaseTextField newCartTitleField;
				newCartLayout.addComponent(newCartTitleField = new BaseTextField());
				newCartTitleField.setRequired(true);
				BaseButton saveButton;
				newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						AvailableActionsParam param = presenter.buildAvailableActionsParam(actionMenuLayout);
						String title = newCartTitleField.getValue();

						try {
							presenter.createNewCartAndAddToItRequested(param.getIds(), title);
							getWindow().close();
						} catch (Exception e) {
							getCurrentView().showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(
						presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						AvailableActionsParam param = presenter.buildAvailableActionsParam(actionMenuLayout);
						presenter.addToCartRequested(param.getIds(), sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow());
				ownedCartsTable.setWidth("100%");
				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout, tabSheet);
				layout.setExpandRatio(tabSheet, 1);
				return layout;
			}

			@Override
			public boolean isVisible() {
				AvailableActionsParam param = presenter.buildAvailableActionsParam(actionMenuLayout);
				return presenter.getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally() && containsOnly(
						param.getSchemaTypeCodes(),
						asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE));
			}

			@Override
			public boolean isEnabled() {
				return isVisible();
			}
		};
		SelectionPanelExtension.setStyles(windowButton);
		AvailableActionsParam param = presenter.buildAvailableActionsParam(actionMenuLayout);
		windowButton.setEnabled(
				presenter.getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally() && containsOnly(param.getSchemaTypeCodes(),
						asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)));
		windowButton.setVisible(isEnabled());
		return windowButton;
	}

	@Override
	public void setSearchExpression(String expression) {
		searchField.setValue(expression);
	}

	@Override
	public String getSearchExpression() {
		return searchField.getValue();
	}

	@Override
	public void addEmptyCriterion() {
		criteria.addEmptyCriterion();
	}

	@Override
	public ConstellioHeader hideAdvancedSearchPopup() {
		popupView.setPopupVisible(false);
		return this;
	}

	@Override
	public List<Criterion> getAdvancedSearchCriteria() {
		return criteria.getSearchCriteria();
	}

	@Override
	public void setAdvancedSearchCriteria(List<Criterion> criteria) {
		this.criteria.setSearchCriteria(criteria);
	}

	@Override
	public void selectAdvancedSearchSchemaType(String schemaTypeCode) {
		if (schemaTypeCode == null || !schemaTypeCode.equals(advancedSearchSchemaTypeField.getValue())) {
			advancedSearchSchemaTypeField.setValue(schemaTypeCode);
		}
		presenter.schemaTypeSelected(schemaTypeCode);
		clearAdvancedSearchButton.setEnabled(true);
	}

	@Override
	public String getAdvancedSearchSchemaType() {
		return presenter.getSchemaType();
	}

	@Override
	public void setAdvancedSearchSchemaType(String schemaTypeCode) {
		criteria.setSchemaType(schemaTypeCode);
	}

	@Override
	public CoreViews navigateTo() {
		return new CoreViews(getUI().getNavigator());
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	public Navigator getNavigator() {
		return UI.getCurrent().getNavigator();
	}

	@Override
	public String getCollection() {
		return getSessionContext().getCurrentCollection();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	public void updateUIContent() {
		ConstellioUI.getCurrent().updateContent();
	}

	protected MenuBar buildCollectionMenu() {
		MenuBar collectionMenu = new BaseMenuBar();
		if (!collections.isEmpty()) {
			collectionMenu.setAutoOpen(true);
			collectionMenu.addStyleName("header-collection-menu");

			SessionContext sessionContext = getSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			String collectionLabel = collectionCodeToLabelConverter.getCollectionCaption(currentCollection);
			Page.getCurrent().setTitle(collectionLabel);

			collectionSubMenu = collectionMenu.addItem("", FontAwesome.DATABASE, null);
			for (final String collection : collections) {
				if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
					String collectionCaption = collectionCodeToLabelConverter.getCollectionCaption(collection);
					MenuItem collectionMenuItem = collectionSubMenu.addItem(collectionCaption, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.collectionClicked(collection);
							List<MenuItem> menuItems = selectedItem.getParent().getChildren();
							for (MenuItem menuItem : menuItems) {
								menuItem.setChecked(false);
							}
							selectedItem.setChecked(true);
						}
					});
					collectionMenuItem.setCheckable(true);
					collectionMenuItem.setChecked(currentCollection.equals(collection));
					collectionButtons.put(collection, collectionMenuItem);
				}
			}
		} else {
			collectionMenu.setVisible(false);
		}
		return collectionMenu;
	}

	private MenuBar buildActionMenu() {
		MenuBar headerMenu = new BaseMenuBar();
		headerMenu.setAutoOpen(true);
		headerMenu.addStyleName("header-action-menu");
		MenuItem headerMenuRoot = headerMenu.addItem($("ConstellioHeader.actions"), FontAwesome.BARS, null);
		actionMenuItems = presenter.getActionMenuItems();
		final Map<NavigationItem, MenuItem> menuItems = new HashMap<>();
		for (final NavigationItem navigationItem : actionMenuItems) {
			MenuItem menuItem = headerMenuRoot.addItem($("ConstellioHeader." + navigationItem.getCode()), new MenuBar.Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					navigationItem.activate(navigate());
				}
			});
			menuItems.put(navigationItem, menuItem);
			updateMenuItem(navigationItem, menuItem);
		}
		ConstellioUI.getCurrent().getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				View oldView = event.getOldView();
				View newView = event.getNewView();
				if (oldView instanceof BaseView && newView instanceof BaseView) {
					for (NavigationItem navigationItem : actionMenuItems) {
						MenuItem menuItem = menuItems.get(navigationItem);
						navigationItem.viewChanged((BaseView) oldView, (BaseView) newView);
						updateMenuItem(navigationItem, menuItem);
					}
				}
			}
		});
		return headerMenu;
	}

	protected void updateMenuItem(NavigationItem navigationItem, MenuItem menuItem) {
		menuItem.setText($("ConstellioHeader." + navigationItem.getCode()));
		ComponentState state = presenter.getStateFor(navigationItem);
		if (navigationItem.getFontAwesome() != null) {
			menuItem.setIcon(navigationItem.getFontAwesome());
		}
		menuItem.setVisible(state.isVisible());
		menuItem.setEnabled(state.isEnabled());
		menuItem.setStyleName(navigationItem.getCode());
	}

	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
	}

	@Override
	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	@Override
	public void setSelectionButtonEnabled(boolean enabled) {
		if (selectionButton != null) {

			selectionButton.setEnabled(enabled);
			setSelectionButtonIcon();
		} else {
			delayedSelectionButtonEnabled = enabled;
		}
	}

	private void setSelectionButtonIcon() {
		Resource icon;
		if (selectionButton.isEnabled()) {
			icon = FontAwesome.CHECK_SQUARE_O;
		} else {
			icon = FontAwesome.SQUARE_O;
		}
		selectionButton.setIcon(icon);
	}

	@Override
	public void setSelectionCount(int selectionCount) {
		this.selectionCount = selectionCount;
		if (selectionButton != null) {
			selectionButton.setBadgeCount(selectionCount);
		}
	}

	@Override
	public void refreshSelectionPanel() {

	}

	public void closeWindow() {
		for (Window window : new ArrayList<>(UI.getCurrent().getWindows())) {
			window.close();
		}
	}

	public void removeRecordsFromPanel(List<String> idList) {
		for (String id : idList) {
			selectionTable.removeItem(id);
		}
		refreshButtons();
	}

	@Override
	public BaseView getCurrentView() {
		return (BaseView) ConstellioUI.getCurrent().getCurrentView();
	}

	public boolean containsOnly(List<String> list, List<String> values) {
		for (String value : list) {
			if (!values.contains(value)) {
				return false;
			}
		}
		return true && list.size() > 0;
	}

	@Override
	public void refreshButtons() {
		actionMenuLayout.removeAllComponents();
		buildSelectionPanelButtons(actionMenuLayout);
	}

	public void updateRecords() {
		selectionTableAdapter.refreshUI();
	}

	@Override
	public void setCurrentCollectionQuietly() {
		String currentCollection = getSessionContext().getCurrentCollection();
		for (Map.Entry<String, MenuItem> entry : collectionButtons.entrySet()) {
			if (currentCollection.equals(entry.getKey())) {
				entry.getValue().setChecked(true);
				String collectionLabel = collectionCodeToLabelConverter.getCollectionCaption(currentCollection);
				Page.getCurrent().setTitle(collectionLabel);
			} else {
				entry.getValue().setChecked(false);
			}
		}
	}

	private Table buildOwnedFavoritesTable(final Window window) {
		final AvailableActionsParam param = presenter.buildAvailableActionsParam(actionMenuLayout);
		List<DefaultFavoritesTable.CartItem> cartItems = new ArrayList<>();
		cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesConstellioHeader", container, presenter.getSchema());
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					presenter.addToDefaultFavoriteRequested(param.getIds());
				} else {
					presenter.addToCartRequested(param.getIds(), cart);
				}
				window.close();
			}
		});
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}


	public ConstellioHeaderPresenter getPresenter() {
		return presenter;
	}
}
