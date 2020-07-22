package com.constellio.app.ui.pages.base;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BasePopupView;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.autocomplete.StringAutocompleteField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.SessionContext.SelectedRecordIdsChangeListener;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
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
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

@SuppressWarnings("serial")
public class ConstellioHeaderImpl extends I18NHorizontalLayout implements ConstellioHeader, SelectedRecordIdsChangeListener, BrowserWindowResizeListener {

	private static final String POPUP_ID = "header-popup";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME = "header-show-advanced-search-button-popup-hidden";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME = "header-show-advanced-search-button-popup-visible";
	//public static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");
	public static final Resource SELECTION_ICON_RESOURCE = FontAwesome.SHOPPING_BASKET;

	private List<String> collections = new ArrayList<>();

	private final ConstellioHeaderPresenter presenter;

	private StringAutocompleteField<String> searchField;
	private WindowButton selectionButton;

	private BasePopupView popupView;

	private Button showDeactivatedMetadatasButton;
	private Button showAdvancedSearchButton;
	private ComboBox advancedSearchSchemaTypeField;
	private ComboBox advancedSearchSchemaField;
	private Component advancedSearchForm;
	private Button clearAdvancedSearchButton;
	private AdvancedSearchCriteriaComponent criteria;

	private Component selectionPanel;
	private RecordListMenuBar selectionActionMenuBar;
	private BaseTable selectionTable;
	private int selectionCount;
	private BaseButton clearSelectionButton;

	private Boolean delayedSelectionButtonEnabled;
	private BaseView currentView;

	private MenuItem collectionSubMenu;
	private CollectionCodeToLabelConverter collectionCodeToLabelConverter = new CollectionCodeToLabelConverter();
	private HashMap<String, MenuItem> collectionButtons = new HashMap<>();
	private Locale locale;

	private List<NavigationItem> actionMenuItems;

	private Boolean lastPhoneMode;

	public ConstellioHeaderImpl() {
		presenter = new ConstellioHeaderPresenter(this);

		Resource logoResource = presenter.getUserLogoResource();
		Image logo = new Image("", logoResource != null ? logoResource : new ThemeResource("images/logo_eim_406x60.png"));
		logo.setWidth("170px");
		logo.setHeightUndefined();
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
		searchField.setWidth("100%");
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

		BaseButton searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		};
		searchButton.addStyleName("header-search-button");
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

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

		I18NHorizontalLayout searchFieldLayout = new I18NHorizontalLayout(searchField, showAdvancedSearchButton, searchButton);
		searchFieldLayout.addStyleName("header-search-field-layout");
		searchFieldLayout.setWidth("100%");
		searchFieldLayout.setExpandRatio(searchField, 1);

		addComponents(logo, searchFieldLayout, collectionMenu, actionMenu, selectionButton,
				popupView);
		//		setComponentAlignment(headerMenu, Alignment.MIDDLE_RIGHT);
		setExpandRatio(searchFieldLayout, 1);
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
					advancedSearchSchemaField.setValue(null);
					advancedSearchSchemaField.setEnabled(false);
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
		//		component.setWidthUndefined();

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
		if (popupView.isPopupVisible() || ResponsiveUtils.isPhone()) {
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
		Component schemaComponent = buildSchemaComponent();
		I18NHorizontalLayout top = new I18NHorizontalLayout(schemaTypeComponent, schemaComponent, addCriterion);
		top.setComponentAlignment(schemaTypeComponent, Alignment.BOTTOM_LEFT);
		top.setComponentAlignment(schemaComponent, Alignment.BOTTOM_LEFT);
		top.setComponentAlignment(addCriterion, Alignment.BOTTOM_RIGHT);
		top.setWidth("100%");

		String caption = presenter.isDeactivatedMetadatasShown()
						 ? $("ConstellioHeader.hideDeactivatedMetadatas")
						 : $("ConstellioHeader.showDeactivatedMetadatas");
		showDeactivatedMetadatasButton = new Button(caption);
		showDeactivatedMetadatasButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.toggleDeactivatedMetadatas();

				String caption = presenter.isDeactivatedMetadatasShown()
								 ? $("ConstellioHeader.hideDeactivatedMetadatas")
								 : $("ConstellioHeader.showDeactivatedMetadatas");
				showDeactivatedMetadatasButton.setCaption(caption);
			}
		});
		showDeactivatedMetadatasButton.addStyleName(ValoTheme.BUTTON_LINK);

		Label criteriaLabel = new Label($("ConstellioHeader.searchCriteriaLabel"));
		criteriaLabel.setWidthUndefined();

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
				advancedSearchSchemaField.setValue(null);
				advancedSearchSchemaField.setEnabled(false);
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

		VerticalLayout paramsUI = new VerticalLayout(top, showDeactivatedMetadatasButton, criteriaLabel, criteria);
		if (isRightToLeft()) {
			paramsUI.setComponentAlignment(showDeactivatedMetadatasButton, Alignment.TOP_RIGHT);
			paramsUI.setComponentAlignment(criteriaLabel, Alignment.TOP_RIGHT);
		}
		VerticalLayout searchUI = new VerticalLayout(paramsUI, bottom);
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

	private Component buildSchemaComponent() {
		Label label = new Label($("AdvancedSearchView.schema"));

		advancedSearchSchemaField = new BaseComboBox();
		advancedSearchSchemaField.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		advancedSearchSchemaField.setNullSelectionAllowed(false);
		advancedSearchSchemaField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				selectAdvancedSearchSchema((String) advancedSearchSchemaField.getValue());
			}
		});
		advancedSearchSchemaField.setEnabled(false);

		I18NHorizontalLayout layout = new I18NHorizontalLayout(label, advancedSearchSchemaField);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	private void populateSchemaComponent(String schemaTypeCode) {
		if (schemaTypeCode == null) {
			return;
		}

		String selectedSchema = presenter.getSchemaSelected();
		advancedSearchSchemaField.removeAllItems();

		advancedSearchSchemaField.addItem("");
		advancedSearchSchemaField.setItemCaption("", $("ConstellioHeader.allSchemas"));

		for (MetadataSchemaVO schema : presenter.getSchemaOfSelectedType()) {
			advancedSearchSchemaField.addItem(schema.getCode());
			String itemCaption = schema.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			advancedSearchSchemaField.setItemCaption(schema.getCode(), itemCaption);
		}

		if (advancedSearchSchemaField.getItemIds().size() > 2) {
			advancedSearchSchemaField.setEnabled(true);
		} else {
			advancedSearchSchemaField.setEnabled(false);
		}

		if (selectedSchema == null || advancedSearchSchemaField.getItem(selectedSchema) == null) {
			advancedSearchSchemaField.select("");
		} else {
			advancedSearchSchemaField.select(selectedSchema);
		}
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
		Page.getCurrent().addBrowserWindowResizeListener(this);
		computeResponsive();
		super.attach();
	}

	@Override
	public void detach() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		sessionContext.removeSelectedRecordIdsChangeListener(this);
		Page.getCurrent().removeBrowserWindowResizeListener(this);
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

		selectionTable = new BaseTable("selection-table") {
			@Override
			public boolean isSelectColumn() {
				return true;
			}

			@Override
			protected SelectionManager newSelectionManager() {
				return new SelectionManager() {
					@Override
					public void selectionChanged(SelectionChangeEvent event) {
						if (event.isAllItemsSelected()) {
							presenter.selectAllClicked();
						} else if (event.isAllItemsDeselected()) {
							presenter.deselectAllClicked();
						} else {
							List<Object> selectedItemIds = event.getSelectedItemIds();
							List<Object> deselectedItemIds = event.getDeselectedItemIds();
							if (selectedItemIds != null) {
								for (Object selectedItemId : selectedItemIds) {
									String recordId = (String) selectedItemId;
									presenter.selectionChanged(recordId, true);
								}
							} else if (deselectedItemIds != null) {
								for (Object deselectedItemId : deselectedItemIds) {
									String recordId = (String) deselectedItemId;
									presenter.selectionChanged(recordId, false);
								}
							}
						}
					}

					@Override
					public List<Object> getAllSelectedItemIds() {
						List<Object> allSelectedItemIds;
						if (isAllItemsSelected()) {
							allSelectedItemIds = new ArrayList<>(getItemIds());
						} else {
							allSelectedItemIds = ensureListValue();
						}
						return allSelectedItemIds;
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

					@SuppressWarnings({"rawtypes"})
					private List<Object> ensureListValue() {
						List<Object> listValue;
						Object objectValue = getValue();
						if (objectValue instanceof List) {
							listValue = (List) objectValue;
						} else {
							listValue = new ArrayList<>();
						}
						return listValue;
					}
				};
			}

		};
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

		clearSelectionButton = new BaseButton($("ConstellioHeader.clearSelection"), FontAwesome.TRASH_O) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.clearSelectionButtonClicked();
			}
		};
		clearSelectionButton.setCaptionVisibleOnPhone(false);
		clearSelectionButton.setCaptionVisibleOnTablet(true);
		clearSelectionButton.addStyleName(ValoTheme.BUTTON_LINK);

		Component selectDeselectAllToggleButton = selectionTable.newSelectDeselectAllToggleButton();
		selectDeselectAllToggleButton.addStyleName(ValoTheme.BUTTON_LINK);

		I18NHorizontalLayout selectionPanelTopLayout = new I18NHorizontalLayout();
		selectionPanelTopLayout.addStyleName("selection-panel-top-layout");
		selectionPanelTopLayout.setWidth("100%");
		selectionPanelTopLayout.setSpacing(true);

		selectionActionMenuBar = buildSelectionPanelMenuBar();
		selectionPanelTopLayout.addComponents(selectDeselectAllToggleButton, clearSelectionButton, selectionActionMenuBar);
		selectionPanelTopLayout.setComponentAlignment(clearSelectionButton, Alignment.TOP_RIGHT);
		selectionPanelTopLayout.setComponentAlignment(selectionActionMenuBar, Alignment.TOP_RIGHT);

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

		selectionPanel.addComponent(selectionPanelTopLayout);
		selectionPanel.addComponent(selectionTable);
		selectionPanel.addComponent(buttonsLayout);
		selectionPanel.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
		selectionPanel.setExpandRatio(selectionTable, 1);

		return selectionPanel;
	}

	private RecordListMenuBar buildSelectionPanelMenuBar() {
		final MenuItemRecordProvider recordProvider = new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return presenter.getSelectedRecords();
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		};

		List<String> excludedActionTypes = Arrays.asList(RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION.name());
		RecordListMenuBar recordListMenuBar = new RecordListMenuBar(recordProvider, $("ConstellioHeader.selectionActions"), excludedActionTypes);
		return recordListMenuBar;
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
		populateSchemaComponent(schemaTypeCode);
		criteria.setSchemaType(schemaTypeCode);
	}

	@Override
	public void selectAdvancedSearchSchema(String schemaCode) {
		if (schemaCode != null && !schemaCode.equals(advancedSearchSchemaField.getValue())) {
			advancedSearchSchemaField.setValue(schemaCode);
		}
		presenter.schemaSelected(schemaCode);
	}

	@Override
	public void setAdvancedSearchSchema(String schemaCode) {
		criteria.setSchemaSelected(schemaCode);
	}

	@Override
	public void setShowDeactivatedMetadatas(boolean shown) {
		criteria.setShowDeactivatedMetadatas(shown);
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
		return ConstellioUI.getCurrent().getNavigator();
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

	private void setCollectionSubMenuCaption() {
		String caption;
		SessionContext sessionContext = getSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		if (currentCollection != null) {
			caption = collectionCodeToLabelConverter.getCollectionCaption(currentCollection);
			int maxWidth = 50;
			if (caption.length() > maxWidth) {
				caption = StringUtils.truncate(caption, maxWidth);
			}
		} else {
			caption = "";
		}
		if (ResponsiveUtils.isDesktop()) {
			collectionSubMenu.setText(caption);
		} else {
			collectionSubMenu.setText("");
		}
	}

	protected MenuBar buildCollectionMenu() {
		MenuBar collectionMenu = new BaseMenuBar();
		collectionMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

		if (!collections.isEmpty()) {
			ArrayList<String> sortedCollections = new ArrayList<>(collections);
			Collections.sort(sortedCollections, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					String collectionCaption1 = AccentApostropheCleaner.removeAccents(collectionCodeToLabelConverter.getCollectionCaption(o1).toLowerCase());
					String collectionCaption2 = AccentApostropheCleaner.removeAccents(collectionCodeToLabelConverter.getCollectionCaption(o2).toLowerCase());
					return collectionCaption1.compareTo(collectionCaption2);
				}
			});
			collectionMenu.setAutoOpen(true);
			collectionMenu.addStyleName("header-collection-menu");

			SessionContext sessionContext = getSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			String collectionLabel = collectionCodeToLabelConverter.getCollectionCaption(currentCollection);
			Page.getCurrent().setTitle(collectionLabel);

			collectionSubMenu = collectionMenu.addItem("", FontAwesome.DATABASE, null);
			setCollectionSubMenuCaption();
			for (final String collection : sortedCollections) {
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
							setCollectionSubMenuCaption();
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
		getNavigator().addViewChangeListener(new ViewChangeListener() {
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
		//		Resource icon;
		//		if (selectionButton.isEnabled()) {
		//			icon = FontAwesome.CHECK_SQUARE_O;
		//		} else {
		//			icon = FontAwesome.SQUARE_O;
		//		}
		selectionButton.setIcon(SELECTION_ICON_RESOURCE);
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
		refreshActionButtons();
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
	public void refreshActionButtons() {
		selectionActionMenuBar.buildMenuItems();
		clearSelectionButton.setVisible(!presenter.getSelectedRecords().isEmpty());
	}

	public void updateRecords() {
		selectionTable.refreshRenderedCells();
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

	public ConstellioHeaderPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		computeResponsive();
	}

	private void computeResponsive() {
		if (lastPhoneMode == null) {
			lastPhoneMode = ResponsiveUtils.isPhone();
			if (ResponsiveUtils.isPhone()) {
				showAdvancedSearchButton.setVisible(false);
			}
		}
		if (lastPhoneMode && !ResponsiveUtils.isPhone()) {
			adjustSearchFieldContent();
			setCollectionSubMenuCaption();
			showAdvancedSearchButton.setVisible(true);
		} else if (!lastPhoneMode && ResponsiveUtils.isPhone()) {
			adjustSearchFieldContent();
			setCollectionSubMenuCaption();
			showAdvancedSearchButton.setVisible(false);
		}
		lastPhoneMode = ResponsiveUtils.isPhone();
	}
}
