package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.breadcrumb.DecommissionBreadcrumbTrail;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeListener;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.SaveSearchListener;
import com.constellio.app.ui.pages.search.SearchViewImpl;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.extensions.app.RMDecommissioningBuilderMenuItemActionsExtension.RMRECORDS_CREATE_DECOMMISSIONING_LIST;
import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningBuilderViewImpl extends SearchViewImpl<DecommissioningBuilderPresenter>
		implements DecommissioningBuilderView {
	public static final String FILING_SPACE = "filing-space";
	public static final String ADMIN_UNIT = "admin-unit";
	public static final String SEARCH = "search";
	public static final String CREATE_LIST = "create-list";

	public static final String DECOMMISSIONING_BUILDER_TYPE = "decommissioning-builder-title";
	public static final String SAVE_SEARCH_DECOMMISSIONING = "save-search-decommissioning";

	private AdvancedSearchCriteriaComponent criteria;
	private Button searchButton;
	private Button addToListButton;
	private LookupRecordField adminUnit;
	private String saveEventId = null;
	//	private MenuItemServices menuItemServices;
	//	private MenuItemFactory menuItemFactory;
	private Button generateDecomlistButon;
	private Button createList;
	private Button buttonAddToList;

	public DecommissioningBuilderViewImpl() {
		presenter = new DecommissioningBuilderPresenter(this);
		presenter.resetFacetAndOrder();
		criteria = new AdvancedSearchCriteriaComponent(presenter);

		adminUnit = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE) {
			@Override
			public boolean isEnabled() {
				return presenter.isAddMode();
			}

			@Override
			protected boolean isClearButtonVisible() {
				return false;
			}
		};

		addSaveSearchListenerList(new SaveSearchListener() {
			@Override
			protected void save(Event event) {
				saveEventId = event.getSavedSearch().getId();
				getUIContext().setAttribute(DECOMMISSIONING_BUILDER_TYPE, presenter.getSearchType().toString());
				getUIContext().setAttribute(SAVE_SEARCH_DECOMMISSIONING, saveEventId);
				setExtraParameters(presenter.getSearchType().toString(), saveEventId);
			}
		});

		//		menuItemServices = new MenuItemServices(getCollection(), getConstellioFactories().getAppLayerFactory());
		//		menuItemFactory = new MenuItemFactory();

		addStyleName("search-decommissioning");
	}

	@Override
	public boolean isSelectionActionMenuBar() {
		return false;
	}

	public void setExtraParameters(String searchType, String saveEventId) {
		Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put(DECOMMISSIONING_BUILDER_TYPE, searchType);
		extraParameters.put(SAVE_SEARCH_DECOMMISSIONING, saveEventId);

		DecommissioningBuilderViewImpl.this.setExtraParameters(extraParameters);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
		super.initBeforeCreateComponents(event);
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		// TODO a ajuster apres le refactor pour les whiteliste de l'api des menu.
		//		MenuItemIdProvider menuItemIdProvider = new MenuItemIdProvider() {
		//			@Override
		//			public List<String> getIds() {
		//				return getSelectedRecordIds();
		//			}
		//
		//			@Override
		//			public LogicalSearchQuery getQuery() {
		//				return null;
		//			}
		//		};
		//
		//		List<MenuItemAction> queryMenuItemActions = menuItemServices.getActionsForRecords(menuItemIdProvider.getIds(),
		//				Collections.emptyList(),
		//				new MenuItemActionBehaviorParams() {
		//					@Override
		//					public BaseView getView() {
		//						return (BaseView) ConstellioUI.getCurrent().getCurrentView();
		//					}
		//
		//					@Override
		//					public Map<String, String> getFormParams() {
		//						return MapUtils.emptyIfNull(ParamUtils.getCurrentParams());
		//					}
		//
		//					@Override
		//					public User getUser() {
		//						return presenter.getUser();
		//					}
		//				});
		//
		//
		//		MenuItemAction generateDecomlistMenuAction = null;
		//
		//		for(MenuItemAction menuItemActionType : queryMenuItemActions) {
		//			if(RMRECORDS_CREATE_DECOMMISSIONING_LIST.equals(menuItemActionType.getType())) {
		//				generateDecomlistMenuAction = menuItemActionType;
		//				break;
		//			}
		//		}
		//
		//		if(generateDecomlistMenuAction != null) {
		//			generateDecomlistButon = menuItemFactory.buildActionButtons(Arrays.asList(generateDecomlistMenuAction), menuItemIdProvider, new CommandCallback() {
		//				@Override
		//				public void actionExecuted(MenuItemAction menuItemAction, Object component) {
		//				}
		//			}).get(0);
		//		}


		createList = new DecommissioningButton($("DecommissioningBuilderView.createDecommissioningList"));
		createList.addStyleName(ValoTheme.BUTTON_LINK);
		createList.addStyleName(CREATE_LIST);

		buttonAddToList = buildAddToListButton();

		updateGenerateDecomListButton();

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				updateGenerateDecomListButton();
			}
		});

		return Arrays.asList(createList, buttonAddToList);
	}

	private void updateGenerateDecomListButton() {
		if (createList != null) {
			createList.setEnabled(!getSelectedRecordIds().isEmpty());
		}

		if (buttonAddToList != null) {
			buttonAddToList.setEnabled(!getSelectedRecordIds().isEmpty());
		}
	}

	@Override
	protected boolean isActionMenuBar() {
		return false;
	}

	@Override
	protected String getTitle() {
		SearchType type = presenter.getSearchType();
		return $("DecommissioningBuilderView.viewTitle." + type);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (presenter.isAddMode()) {
					navigate().to(RMViews.class).decommissioning();
				} else if (presenter.getDecommissioningList().getDecommissioningListType().isFolderList()) {
					navigate().to(RMViews.class).displayDecommissioningList(presenter.getDecommissioningList().getId());
				} else {
					navigate().to(RMViews.class).displayDocumentDecommissioningList(presenter.getDecommissioningList().getId());
				}
			}
		};
	}

	@Override
	public void addEmptyCriterion() {
		criteria.addEmptyCriterion();
	}

	@Override
	public void setCriteriaSchemaType(String schemaType) {
		criteria.setSchemaType(schemaType);
	}

	@Override
	public void setSearchCriteria(List<Criterion> criteria) {
		this.criteria.setSearchCriteria(criteria);
	}

	@Override
	public void setAdministrativeUnit(String administrativeUnitID) {
		this.adminUnit.setValue(administrativeUnitID);
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return criteria.getSearchCriteria();
	}



	@Override
	protected Component buildSearchUI() {
		Button addCriterion = new Button($("DecommissioningBuilderView.addCriterion"));
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);
		addCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});

		HorizontalLayout horizontalLayout = buildAdministrativeUnitComponent();
		HorizontalLayout top = new HorizontalLayout(horizontalLayout, addCriterion);
		top.setExpandRatio(horizontalLayout, 1);
		top.setSizeFull();
		addCriterion.setWidth("170px");
		addCriterion.addStyleName("align: right");
		top.setSpacing(true);
		top.setComponentAlignment(addCriterion, Alignment.BOTTOM_RIGHT);
		top.setWidth("100%");

		criteria.setWidth("100%");

		searchButton = new Button($("DecommissioningBuilderView.search"));
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.addStyleName(SEARCH);
		searchButton.setEnabled(presenter.mustDisplayResults());
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested();
			}
		});

		VerticalLayout searchUI = new VerticalLayout(top, criteria, searchButton);
		searchUI.addStyleName("decommisioning-list-search-ui");
		searchUI.setSpacing(true);

		searchButton.setEnabled(adminUnit.getValue() != null);

		return searchUI;
	}

	private HorizontalLayout buildAdministrativeUnitComponent() {
		Label label = new Label($("DecommissioningBuilderView.administrativeUnit"));
		adminUnit.setRequired(true);
		adminUnit.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				searchButton.setEnabled(adminUnit.getValue() != null);
				presenter.administrativeUnitSelected((String) adminUnit.getValue());
				presenter.saveTemporarySearch(false);
			}
		});
		adminUnit.addStyleName(ADMIN_UNIT);

		HorizontalLayout layout = new HorizontalLayout(label, adminUnit);
		label.setWidth("160px");
		adminUnit.setSizeFull();
		layout.setExpandRatio(adminUnit, 1);
		layout.setSizeFull();
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}

	public class DecommissioningButton extends WindowButton {
		public static final String TITLE = "dl-title";
		public static final String DESCRIPTION = "dl-description";

		@PropertyId("title") private BaseTextField title;
		@PropertyId("description") private BaseTextArea description;

		public DecommissioningButton(String caption) {
			super(caption, caption);
		}

		@Override
		protected Component buildWindowContent() {
			title = new BaseTextField($("DecommissioningBuilderView.title"));
			title.setRequired(true);
			title.setId(TITLE);

			description = new BaseTextArea($("DecommissioningBuilderView.description"));
			description.setId(DESCRIPTION);

			return new BaseForm<DecommissioningListParams>(
					new DecommissioningListParams(), this, title, description) {
				@Override
				protected void saveButtonClick(DecommissioningListParams params) {
					getWindow().close();
					params.setSelectedRecordIds(getSelectedRecordIds());
					if (presenter.isDecommissioningListWithSelectedFolders()) {
						params.setFolderDetailStatus(FolderDetailStatus.SELECTED);
					} else {
						params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);
					}
					presenter.decommissioningListCreationRequested(params);
				}

				@Override
				protected void cancelButtonClick(DecommissioningListParams params) {
					getWindow().close();
				}
			};
		}

		@Override
		public boolean isVisible() {
			return presenter.isAddMode();
		}
	}

	private Button buildAddToListButton() {
		addToListButton = new Button($("DecommissioningBuilderView.addToList")) {
			@Override
			public boolean isVisible() {
				return !presenter.isAddMode();
			}
		};
		addToListButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addToListButtonClicked(getSelectedRecordIds());
			}
		});
		return addToListButton;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new DecommissionBreadcrumbTrail(getTitle(), presenter.getSearchType(), null, presenter.decommissioningListId, this, false);
	}

	public SearchType getSearchType() {
		return presenter.getSearchType();
	}

	public String getAdminUnitId() {
		return presenter.getAdminUnitId();
	}

	@Override
	public List<String> menuItemToExcludeInSelectionMenu() {
		return Arrays.asList(RMRECORDS_CREATE_DECOMMISSIONING_LIST);
	}

}
