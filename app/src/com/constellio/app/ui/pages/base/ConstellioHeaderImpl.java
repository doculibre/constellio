package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.SelectionPanelExtension;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.BadgeButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BasePopupView;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.SessionContext.SelectedRecordIdsChangeListener;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.wrappers.Collection;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class ConstellioHeaderImpl extends HorizontalLayout implements ConstellioHeader {
	
	private static final String POPUP_ID = "header-popup";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME = "header-show-advanced-search-button-popup-hidden";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME = "header-show-advanced-search-button-popup-visible";

	private List<String> collections = new ArrayList<>();

	private final ConstellioHeaderPresenter presenter;
	
	private TextField searchField;
	private BadgeButton selectionButton;
	
	private BasePopupView popupView;
	
	private Button showAdvancedSearchButton;
	private ComboBox advancedSearchSchemaTypeField;
	private Component advancedSearchForm;
	private Button clearAdvancedSearchButton;
	private AdvancedSearchCriteriaComponent criteria;
	
	private Component selectionPanel;
	
	private int selectionCount;
	
	private Boolean delayedSelectionButtonEnabled;
	
    private CollectionCodeToLabelConverter collectionCodeToLabelConverter = new CollectionCodeToLabelConverter();

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

		searchField = new BaseTextField();
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
		addSelectionListener();
		selectionPanel = buildSelectionPanel();

		addComponents(logo, searchField, showAdvancedSearchButton, searchButton, collectionMenu, actionMenu, selectionButton, popupView);
//		setComponentAlignment(headerMenu, Alignment.MIDDLE_RIGHT);
		setSizeFull();

		adjustSearchFieldContent();

		getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
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
			if (popupView.getContent().getPopupComponent() != selectionPanel) {
				BasePopupView newPopupView = newPopupView(selectionPanel);
				replaceComponent(popupView, newPopupView);
				popupView = newPopupView;
			}
			popupView.setPopupVisible(true, false);
		} else {
			popupView.setPopupVisible(false, false);
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

		HorizontalLayout top = new HorizontalLayout(buildSchemaTypeComponent(), addCriterion);
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

		HorizontalLayout bottom = new HorizontalLayout(advancedSearch, clearAdvancedSearchButton, savedSearches);
		bottom.addStyleName("header-popup-clear-and-search-buttons");
		bottom.setSpacing(true);

		VerticalLayout searchUI = new VerticalLayout(top, criteria, bottom);
		searchUI.setSpacing(true);
		return searchUI;
	}

	private Component buildSchemaTypeComponent() {
		Label label = new Label($("AdvancedSearchView.type"));

		advancedSearchSchemaTypeField = new ComboBox();
		for (MetadataSchemaTypeVO schemaType : presenter.getSchemaTypes()) {
			advancedSearchSchemaTypeField.addItem(schemaType.getCode());
			String itemCaption = schemaType.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
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

		HorizontalLayout layout = new HorizontalLayout(label, advancedSearchSchemaTypeField);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}
	
	private BadgeButton buildSelectionButton() {
		BadgeButton selectionButton = new BadgeButton($("ConstellioHeader.selection"), selectionCount) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.selectionButtonClicked();
			}
		};
		selectionButton.addStyleName("header-selection-button");
		if (delayedSelectionButtonEnabled != null) {
			selectionButton.setEnabled(delayedSelectionButtonEnabled);
		}
		return selectionButton;
	}
	
	private void addSelectionListener() {
		SessionContext sessionContext = getSessionContext();
		sessionContext.addSelectedRecordIdsChangeListener(new SelectedRecordIdsChangeListener() {
			@Override
			public void selectionCleared() {
				presenter.selectedRecordsCleared();
			}
			
			@Override
			public void recordIdRemoved(String recordId) {
				presenter.selectedRecordIdRemoved(recordId);
			}
			
			@Override
			public void recordIdAdded(String recordId) {
				presenter.selectedRecordIdAdded(recordId);
			}
		});
	}
	
	@SuppressWarnings({ "unchecked" })
	private Component buildSelectionPanel() {
		VerticalLayout selectionPanel = new VerticalLayout();
		selectionPanel.setSpacing(true);
//		selectionPanel.setWidth("100%");
		
		HorizontalLayout selectionLayout = new HorizontalLayout();
		selectionLayout.setSpacing(true);
		selectionLayout.setWidth("100%");
		selectionLayout.addStyleName("header-popup-selection-panel");
		
		ContainerAdapter<IndexedContainer> container = new ContainerAdapter<IndexedContainer>(new IndexedContainer(), true) {
			@Override
			public boolean isSelected(Object itemId) {
				String recordId = (String) itemId;
				return presenter.isSelected(recordId);
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				String recordId = (String) itemId;
				presenter.selectionChanged(recordId, selected);
			}
		}; 
		Table selectionTable = new Table();
		selectionTable.setContainerDataSource(container);
		selectionTable.addContainerProperty("recordId", ReferenceDisplay.class, null);
		selectionTable.setColumnHeader(ContainerAdapter.SELECT_PROPERTY_ID, "");
		selectionTable.setWidth("100%");
		selectionTable.setColumnExpandRatio("recordId", 1);
		selectionTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		
		List<String> selectedRecordIds = getSessionContext().getSelectedRecordIds();
		for (String selectedRecordId : selectedRecordIds) {
			ReferenceDisplay referenceDisplay = new ReferenceDisplay(selectedRecordId);
			Item item = selectionTable.addItem(selectedRecordId);
			item.getItemProperty("recordId").setValue(referenceDisplay);
		}

		VerticalLayout actionMenuLayout = new VerticalLayout();
		actionMenuLayout.addStyleName("header-selection-panel-actions");
		buildSelectionPanelButtons(actionMenuLayout);

		Button clearSelectionButton = new BaseButton($("ConstellioHeader.clearSelection")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.clearSelectionButtonClicked();
			}
		};
		SelectionPanelExtension.setStyles(clearSelectionButton);
		actionMenuLayout.addComponent(clearSelectionButton);
		
		VerticalLayout selectionActionMenu = new VerticalLayout();
		selectionActionMenu.setWidth("200px");
		selectionActionMenu.setSpacing(true);
		selectionActionMenu.addComponent(actionMenuLayout);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		selectionLayout.addComponent(selectionTable);
		selectionLayout.addComponent(selectionActionMenu);
		
		selectionPanel.addComponent(selectionLayout);
		selectionPanel.addComponent(buttonsLayout);
		selectionPanel.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
		
		selectionLayout.setExpandRatio(selectionTable, 1);
		selectionLayout.setComponentAlignment(selectionActionMenu, Alignment.TOP_RIGHT);
		
		return selectionPanel;
	}

	private void buildSelectionPanelButtons(VerticalLayout actionMenuLayout) {
		WindowButton addToCartButton = buildAddToCartButton();
		SelectionPanelExtension.setStyles(addToCartButton);
		actionMenuLayout.addComponent(addToCartButton);
		presenter.buildSelectionPanelActionButtons(actionMenuLayout);

	}

	private WindowButton buildAddToCartButton() {
		WindowButton windowButton = new WindowButton($("ConstellioHeader.selection.actions.addToCart"), $("ConstellioHeader.selection.actions.addToCart")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();

				HorizontalLayout newCartLayout = new HorizontalLayout();
				newCartLayout.setSpacing(true);
				newCartLayout.addComponent(new Label($("CartView.newCart")));
				final BaseTextField newCartTitleField;
				newCartLayout.addComponent(newCartTitleField = new BaseTextField());
				BaseButton saveButton;
				newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.createNewCartAndAddToItRequested(newCartTitleField.getValue());
						getWindow().close();
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				final RecordVOLazyContainer ownedCartsContainer = new RecordVOLazyContainer(presenter.getOwnedCartsDataProvider());
				RecordVOTable ownedCartsTable = new RecordVOTable($("CartView.ownedCarts"), ownedCartsContainer);
				ownedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(getSessionContext().getSelectedRecordIds(), ownedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(getSessionContext().getSelectedRecordIds(), sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				ownedCartsTable.setPageLength(Math.min(15, ownedCartsContainer.size()));
				ownedCartsTable.setWidth("100%");
				sharedCartsTable.setPageLength(Math.min(15, ownedCartsContainer.size()));
				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout,tabSheet);
				return layout;
			}
		};
		SelectionPanelExtension.setStyles(windowButton);
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
		MenuBar collectionMenu = new MenuBar();
		if (!collections.isEmpty()) {
			collectionMenu.setAutoOpen(true);
			collectionMenu.addStyleName("header-collection-menu");

			SessionContext sessionContext = getSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			String collectionLabel = collectionCodeToLabelConverter.getCollectionCaption(currentCollection);
			Page.getCurrent().setTitle(collectionLabel);

			MenuItem collectionSubMenu = collectionMenu.addItem("", FontAwesome.DATABASE, null);
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
				}
			}
		} else {
			collectionMenu.setVisible(false);
		}
		return collectionMenu;
	}
	
	private MenuBar buildActionMenu() {
		MenuBar headerMenu = new MenuBar();
		headerMenu.setAutoOpen(true);
		headerMenu.addStyleName("header-action-menu");
		MenuItem headerMenuRoot = headerMenu.addItem($("ConstellioHeader.actions"), FontAwesome.BARS, null);
		for (final NavigationItem item : presenter.getActionMenuItems()) {
			ComponentState state = presenter.getStateFor(item);

			MenuItem menuItem = headerMenuRoot.addItem($("ConstellioHeader." + item.getCode()), new MenuBar.Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					item.activate(navigate());
				}
			});
			if (item.getFontAwesome() != null) {
				menuItem.setIcon(item.getFontAwesome());
			}
			menuItem.setVisible(state.isVisible());
			menuItem.setEnabled(state.isEnabled());
			menuItem.setStyleName(item.getCode());
		}	
		return headerMenu;
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
			selectionButton.setCount(selectionCount);
		}
	}

	@Override
	public void refreshSelectionPanel() {

	}
}
