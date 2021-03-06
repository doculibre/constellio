package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
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
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.dao.services.Stats;
import com.constellio.model.entities.security.global.UserComponentStatus;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class UserSecurityManagementImpl extends BaseViewImpl implements SecurityManagement {

	private final VerticalLayout mainLayout;
	private VerticalLayout contentLayout;
	private ViewableRecordVOTablePanel viewerPanel;
	private BaseButton clearSearchButton;
	private SearchButton searchButton;
	private OptionGroup activeGroup;
	private StringAutocompleteField<String> searchField;
	private VerticalLayout searchLayout;

	private RecordVODataProvider userDataProvider;
	private UserSecurityManagementPresenter presenter;
	private OptionGroup.ValueChangeListener optionValueChangeListener;

	public UserSecurityManagementImpl() {
		this(null);
	}

	public UserSecurityManagementImpl(RecordVO recordVO) {
		presenter = Stats.compilerFor(getClass().getSimpleName()).log(() -> {
			return new UserSecurityManagementPresenter(this, recordVO);
		});
		presenter.forParams(null);
		mainLayout = new VerticalLayout();
		this.buildMainComponent(null);
		presenter.viewAssembled();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout.addStyleName("user-security-management");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);

		activeGroup = new OptionGroup();
		activeGroup.addItem(UserComponentStatus.ACTIVE);
		activeGroup.setItemCaption(UserComponentStatus.ACTIVE, $("UserCredentialStatus." + UserComponentStatus.ACTIVE.getCode()));
		activeGroup.addItem(UserComponentStatus.INACTIVE);
		activeGroup.setItemCaption(UserComponentStatus.INACTIVE, $("UserCredentialStatus." + UserComponentStatus.INACTIVE.getCode()));
		activeGroup.select(UserComponentStatus.ACTIVE);

		activeGroup.addValueChangeListener(optionValueChangeListener = new OptionGroup.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean active = activeGroup.getValue().equals(UserComponentStatus.ACTIVE); 
				presenter.activeSelectionChanged(active);
			}
		});
		activeGroup.addStyleName("horizontal");

		contentLayout = new VerticalLayout();
		contentLayout.setWidth("100%");
		mainLayout.addComponent(contentLayout);

		return mainLayout;
	}

	@Override
	protected String getTitle() {
		return $("ListCollectionUserView.viewTitle");
	}

	@Override
	public RecordVODataProvider getDataProvider() {
		return userDataProvider;
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		this.userDataProvider = dataProvider;
	}

	@Override
	public VerticalLayout createTabLayout() {
		final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(userDataProvider);
		viewerPanel = createViewerPanel(recordVOContainer);

		if (clearSearchButton == null) {
			clearSearchButton = new LinkButton($("CollectionSecurityManagement.clearSearch")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.clearSearchRequested();
					searchField.setValue("");
				}
			};
			clearSearchButton.addStyleName("folder-search-clear");
		}

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
			searchButton = new SearchButton() {
				@Override
				protected void buttonClick(ClickEvent event) {
					String searchValue = searchField.getValue();
					presenter.searchRequested(searchValue);
				}
			};
			searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			searchButton.setIconOnly(true);
			OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
				@Override
				public void onEnterKeyPressed() {
					String searchValue = searchField.getValue();
					presenter.searchRequested(searchValue);
				}
			};
			onEnterHandler.installOn(searchField);
		}

		if (searchLayout == null) {
			searchLayout = new VerticalLayout();
			searchLayout.addStyleName("folder-search-layout");
			searchLayout.setSpacing(true);
			searchLayout.setWidth("50%");

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
		searchToggleAndFieldsLayout.addComponent(searchLayout);
		searchToggleAndFieldsLayout.addComponent(viewerPanel);
		searchToggleAndFieldsLayout.setSpacing(true);
		mainLayout.replaceComponent(contentLayout, contentLayout = searchToggleAndFieldsLayout);

		viewerPanel.setSelectionActionButtons();
		return contentLayout;
	}

	private ViewableRecordVOTablePanel createViewerPanel(RecordVOLazyContainer recordVOContainer) {
		ViewableRecordVOTablePanel panel = new ViewableRecordVOTablePanel(recordVOContainer) {

			@Override
			public boolean isMenuBarColumn() {
				return true;
			}

			@Override
			protected boolean isSelectColumn() {
				return true;
			}

			@Override
			public void setQuickActionButtonsVisible(boolean visible) {
				super.setQuickActionButtonsVisible(visible);
				UserSecurityManagementImpl.this.setQuickActionButtonsVisible(visible);
			}

			//TODO
			// be able to show user's profile picture (or group)
			@Override
			protected boolean isShowThumbnailCol() {
				return false;
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
		panel.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				//event.getItem();
				RecordVOItem recordItem = (RecordVOItem) event.getItem();
				UserVO user = (UserVO) recordItem.getRecord();
				presenter.displayButtonClicked(user);
			}
		});
		panel.setCountCaption($("UserSecurityManagement.users", recordVOContainer.size()));
		return panel;
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
		final RecordVOLazyContainer recordVOContainer = new RecordVOLazyContainer(userDataProvider);
		ViewableRecordVOTablePanel newViewerPanel = createViewerPanel(recordVOContainer);
		contentLayout.replaceComponent(viewerPanel, viewerPanel = newViewerPanel);
		viewerPanel.setSelectionActionButtons();
	}

}
