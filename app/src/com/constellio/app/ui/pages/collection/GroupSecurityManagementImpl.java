package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.autocomplete.StringAutocompleteField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.dao.services.Stats;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupSecurityManagementImpl extends BaseViewImpl implements SecurityManagement {

	public static final String STYLE_NAME = "display-folder";

	private VerticalLayout mainLayout;
	private VerticalLayout contentLayout;
	private ViewableRecordVOTablePanel viewerPanel;
	private BaseButton clearSearchButton;
	private SearchButton searchButton;
	private OptionGroup activeGroup;
	private StringAutocompleteField<String> searchField;
	private VerticalLayout searchLayout;

	private RMModuleExtensions rmModuleExtensions;

	private RecordVODataProvider groupDataProvider;
	private GroupSecurityManagementPresenter presenter;
	private TabSheet.SelectedTabChangeListener selectedTabChangeListener;
	private OptionGroup.ValueChangeListener optionValueChangeListener;
	private boolean nestedView;
	private boolean dragNDropAllowed;
	private boolean dragRowsEnabled;

	public GroupSecurityManagementImpl() {
		this(null);
	}

	public GroupSecurityManagementImpl(RecordVO recordVO) {
		presenter = Stats.compilerFor(getClass().getSimpleName()).log(() -> {
			return new GroupSecurityManagementPresenter(this, recordVO);
		});
		presenter.forParams(null);
		this.buildMainComponent(null);
		rmModuleExtensions = getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(getCollection()).forModule(ConstellioRMModule.ID);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.addStyleName("display-folder-view");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		activeGroup = new OptionGroup();
		activeGroup.addItem(GlobalGroupStatus.ACTIVE);
		activeGroup.setItemCaption(GlobalGroupStatus.ACTIVE, $("UserCredentialStatus.a"));
		activeGroup.addItem(GlobalGroupStatus.INACTIVE);
		activeGroup.setItemCaption(GlobalGroupStatus.INACTIVE, $("UserCredentialStatus.i"));
		activeGroup.select(GlobalGroupStatus.ACTIVE);

		activeGroup.addValueChangeListener(optionValueChangeListener = new OptionGroup.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (activeGroup.getValue().equals(GlobalGroupStatus.ACTIVE)) {
					presenter.setActive(true);
				} else {
					presenter.setActive(false);
				}
				presenter.clearSearch();

			}
		});

		contentLayout = new VerticalLayout();
		contentLayout.setWidth("100%");
		mainLayout = new VerticalLayout(contentLayout);

		return mainLayout;
	}

	@Override
	protected String getTitle() {
		return $("ListCollectionGroupView.viewTitle");
	}

	@Override
	public RecordVODataProvider getDataProvider() {
		return groupDataProvider;
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		this.groupDataProvider = dataProvider;
	}

	@Override
	public VerticalLayout createTabLayout() {

		final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(groupDataProvider);
		viewerPanel = createViewerPanel(recordVOContainer);

		viewerPanel.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {

			}
		});
		viewerPanel.addStyleName("folder-content-table");

		if (clearSearchButton == null) {
			clearSearchButton = new LinkButton($("CollectionSecurityManagement.clearSearch")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.clearSearch();
					searchField.setValue("");
				}
			};
			clearSearchButton.addStyleName("folder-search-clear");
		}

		BaseButton searchListButton = new LinkButton($("CollectionSecurityManagement.showSearchInGroups")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (searchLayout != null) {
					if (searchLayout.isVisible()) {
						setCaption($("CollectionSecurityManagement.showSearchInGroups"));
					} else {
						setCaption($("CollectionSecurityManagement.hideSearchInGroups"));
					}
					searchLayout.setVisible(!searchLayout.isVisible());
					searchField.focus();
				}
			}
		};
		searchListButton.addStyleName("search-in-folder-button");
		if (searchField == null) {
			searchField = new StringAutocompleteField<String>(new StringAutocompleteField.AutocompleteSuggestionsProvider<String>() {
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
			searchButton = new SearchButton() {
				@Override
				protected void buttonClick(ClickEvent event) {
					String value = searchField.getValue();
					presenter.changeGroupDataProvider(value);

				}
			};
			searchButton.addStyleName("folder-search-button");
			searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			searchButton.setIconOnly(true);
			OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
				@Override
				public void onEnterKeyPressed() {
					String value = searchField.getValue();
					presenter.changeGroupDataProvider(value);
				}
			};
			onEnterHandler.installOn(searchField);
		}

		if (searchLayout == null) {
			searchLayout = new VerticalLayout();
			searchLayout.addStyleName("folder-search-layout");
			searchLayout.setSpacing(true);
			searchLayout.setWidth("50%");
			searchLayout.setVisible(false);

			I18NHorizontalLayout searchFieldAndButtonLayout = new I18NHorizontalLayout(searchField, searchButton);
			searchFieldAndButtonLayout.addStyleName("folder-search-field-and-button-layout");
			searchFieldAndButtonLayout.setWidth("100%");
			searchFieldAndButtonLayout.setExpandRatio(searchField, 1);

			I18NHorizontalLayout extraFieldsSearchLayout = new I18NHorizontalLayout(clearSearchButton);
			extraFieldsSearchLayout.addStyleName("folder-search-extra-fields-layout");
			extraFieldsSearchLayout.setSpacing(true);

			searchLayout.addComponents(searchFieldAndButtonLayout, extraFieldsSearchLayout);
		}

		VerticalLayout searchToggleAndFieldsLayout = new VerticalLayout();
		searchToggleAndFieldsLayout.addStyleName("search-folder-toggle-and-fields-layout");
		searchToggleAndFieldsLayout.addComponent(activeGroup);
		searchToggleAndFieldsLayout.addComponent(searchListButton);
		searchToggleAndFieldsLayout.addComponent(searchLayout);
		searchToggleAndFieldsLayout.addComponent(viewerPanel);
		mainLayout.replaceComponent(contentLayout, contentLayout = searchToggleAndFieldsLayout);

		viewerPanel.setSelectionActionButtons();

		return contentLayout;
	}

	private ViewableRecordVOTablePanel createViewerPanel(RecordVOLazyContainer recordVOContainer) {
		return new ViewableRecordVOTablePanel(recordVOContainer) {
			@Override
			protected boolean isSelectColumn() {
				return !nestedView;
			}

			@Override
			public boolean isNested() {
				return nestedView;
			}

			@Override
			public void setQuickActionButtonsVisible(boolean visible) {
				super.setQuickActionButtonsVisible(visible);
				GroupSecurityManagementImpl.this.setQuickActionButtonsVisible(visible);
			}

			@Override
			public boolean isDropSupported() {
				return dragNDropAllowed;
			}

			//TODO
			// be able to show group's profile picture (or group)
			@Override
			protected boolean isShowThumbnailCol() {
				return false;
			}

			@Override
			public boolean isRowDragSupported() {
				return !isNested() && dragRowsEnabled;
			}

			@Override
			protected void recordsDroppedOn(List<RecordVO> sourceRecordVOs, RecordVO targetRecordVO,
											Boolean above) {
				if (dragNDropAllowed) {
					presenter.recordsDroppedOn(sourceRecordVOs, targetRecordVO);
				}
			}

			@Override
			protected SelectionManager newSelectionManager() {
				return new SelectionManager() {

					private Set<Object> selectedItemIds = new HashSet<>();

					@Override
					public List<Object> getAllSelectedItemIds() {
						return new ArrayList<>(selectedItemIds);
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
						RecordVO recordVO = recordVOContainer.getRecordVO((int) itemId);
						return presenter.isSelected(recordVO);
					}

					@Override
					public void selectionChanged(SelectionChangeEvent event) {

						if (event.isAllItemsSelected()) {
							selectedItemIds.addAll(getRecordVOContainer().getItemIds());
							presenter.selectAllClicked();
						} else if (event.isAllItemsDeselected()) {
							selectedItemIds.clear();
							presenter.deselectAllClicked();
						} else if (event.getSelectedItemIds() != null) {
							List<Object> selectedItemIds = event.getSelectedItemIds();
							for (Object selectedItemId : selectedItemIds) {
								this.selectedItemIds.add(selectedItemId);
								RecordVO recordVO = getRecordVO(selectedItemId);
								presenter.recordSelectionChanged(recordVO, true);
							}
						} else if (event.getDeselectedItemIds() != null) {
							List<Object> deselectedItemIds = event.getDeselectedItemIds();
							for (Object deselectedItemId : deselectedItemIds) {
								this.selectedItemIds.remove(deselectedItemId);
								RecordVO recordVO = getRecordVO(deselectedItemId);
								presenter.recordSelectionChanged(recordVO, false);
							}
						}
					}
				};
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}


	@Override
	public void reloadContent() {
		final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(groupDataProvider);
		ViewableRecordVOTablePanel newViewerPanel = createViewerPanel(recordVOContainer);
		contentLayout.replaceComponent(viewerPanel, viewerPanel = newViewerPanel);
		viewerPanel.setSelectionActionButtons();
	}
}
