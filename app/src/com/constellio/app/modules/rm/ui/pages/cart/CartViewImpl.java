package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer.RecordVOLazyQueryFactory.RecordVOWithDistinctSchemaItem;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.google.common.base.Strings;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class CartViewImpl extends BaseViewImpl implements CartView {

	private static Logger LOGGER = LoggerFactory.getLogger(CartViewImpl.class);
	private boolean isNested;
	private final CartPresenter presenter;
	private CartTabLayout folderLayout;
	private CartTabLayout documentLayout;
	private CartTabLayout containerLayout;
	private Table folderTable;
	private Table documentTable;
	private Table containerTable;
	private BaseTextField folderFilterField;
	private BaseTextField documentFilterField;
	private BaseTextField containerFilterField;
	private String currentSchemaType;
	private VerticalLayout mainLayout;
	private ActionMenuDisplay folderActionMenu;
	private ActionMenuDisplay documentActionMenu;
	private ActionMenuDisplay containerActionMenu;
	private SelectDeselectAllButton folderSelectDeselectAllButton;
	private SelectDeselectAllButton documentSelectDeselectAllButton;
	private SelectDeselectAllButton containerSelectDeselectAllButton;
	private List<CheckBox> allFoldersCheckBoxes;
	private List<CheckBox> allDocumentsCheckBoxes;
	private List<CheckBox> allContainersCheckBoxes;
	private boolean allSelected;

	public CartViewImpl() {
		this(null, false);
	}

	public CartViewImpl(String cartId, boolean isNested) {
		this.isNested = isNested;
		presenter = new CartPresenter(cartId, this);
		allFoldersCheckBoxes = new ArrayList<>();
		allDocumentsCheckBoxes = new ArrayList<>();
		allContainersCheckBoxes = new ArrayList<>();
		allSelected = false;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
		currentSchemaType = Folder.SCHEMA_TYPE;
	}

	@Override
	protected String getTitle() {
		return "";
	}

	protected String getBreadCrumbTitle() {
		if (presenter.isDefaultCart()) {
			return $("CartView.defaultFavoritesViewTitle");
		} else {
			return $("CartView.viewTitle");
		}
	}

	@Override
	protected String getActionMenuBarCaption() {
		return $("CartView.cartActions");
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		MenuItemServices menuItemServices = new MenuItemServices(this.getCollection(), getConstellioFactories().getAppLayerFactory());
		Cart cart = presenter.getCart();
		Record record = null;
		if (cart != null) {
			record = cart.getWrappedRecord();
		} else {
			record = new RecordImpl(presenter.getCartMetadataSchema(), presenter.getCurrentUser().getId());
		}

		List<String> excludedActionTypes = Arrays.asList(RMRecordsMenuItemActionType.RMRECORDS_ADD_CART.name());

		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				menuItemServices.getActionsForRecord(record, excludedActionTypes, new MenuItemActionBehaviorParams() {
					@Override
					public BaseView getView() {
						return (BaseView) ConstellioUI.getCurrent().getCurrentView();
					}

					@Override
					public RecordVO getRecordVO() {
						return presenter.getCartAsRecordVO();
					}

					@Override
					public Map<String, String> getFormParams() {
						return MapUtils.emptyIfNull(ParamUtils.getCurrentParams());
					}

					@Override
					public User getUser() {
						return presenter.getCurrentUser();
					}

					@Override
					public boolean isContextualMenu() {
						return true;
					}
				})
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(final ActionMenuDisplay defaultActionMenuDisplay) {

		ActionMenuDisplay actionMenuDisplay = new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return () -> new MenuItemRecordProvider() {
					@Override
					public List<Record> getRecords() {
						return presenter.getAllCartItemRecords();
					}

					@Override
					public LogicalSearchQuery getQuery() {
						return null;
					}
				};
			}

			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getCartMetadataSchema().getSchemaType()::getCode;
			}

			@Override
			public boolean isQuickActionsAreVisible() {
				return false;
			}

			@Override
			public void actionExecuted(MenuItemAction menuItemAction, Object component) {
				Button button = (Button) component;
				button.setEnabled(menuItemAction.getState().getStatus() != MenuItemActionStateStatus.DISABLED);
				button.setEnabled(menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE);
			}
		};

		return actionMenuDisplay;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getBreadCrumbTitle(), false) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				if (presenter.havePermisionToGroupCart()) {
					return Arrays.asList(new IntermediateBreadCrumbTailItem() {
						@Override
						public String getTitle() {
							return $(CartsListViewImpl.TITLE);
						}

						@Override
						public void activate(Navigation navigate) {
							navigate.to(RMViews.class).listCarts();
						}

						@Override
						public boolean isEnabled() {
							return true;
						}
					});
				} else {
					return new ArrayList<>();
				}
			}
		};
	}

	@Override
	protected Component buildActionMenu(ViewChangeEvent event) {
		if (isNested) {
			return null;
		}
		return super.buildActionMenu(event);
	}

	private HorizontalLayout buildFolderFilterAndMenuBarComponent() {
		HorizontalLayout filterAndMenuBarComponent = new HorizontalLayout();
		filterAndMenuBarComponent.setSpacing(true);
		folderSelectDeselectAllButton = new SelectDeselectAllButton($("CartView.selectAllFolders"), $("CartView.deselectAllFolders")) {
			@Override
			protected void onSelectAll(ClickEvent event) {
				allSelected = true;
				presenter.setAllRecordsSelected(Folder.SCHEMA_TYPE);
				for (CheckBox checkBox : allFoldersCheckBoxes) {
					checkBox.setValue(true);
				}
				allSelected = false;
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				allSelected = false;
				presenter.emptySelectedRecords();
				for (CheckBox checkBox : allFoldersCheckBoxes) {
					checkBox.setValue(false);
				}
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void buttonClickCallBack(boolean selectAllMode) {

			}
		};

		folderSelectDeselectAllButton.addStyleName(ValoTheme.BUTTON_LINK);
		folderFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.folderFilterButtonClicked();
			}
		};

		folderActionMenu = buildActionMenuDisplay();

		filterAndMenuBarComponent.addComponents(folderFilterField, filterButton, folderSelectDeselectAllButton, folderActionMenu);

		return filterAndMenuBarComponent;
	}

	private HorizontalLayout buildDocumentFilterAndMenuBarComponent() {
		HorizontalLayout filterAndMenuBarComponent = new HorizontalLayout();
		documentSelectDeselectAllButton = new SelectDeselectAllButton($("CartView.selectAllDocuments"), $("CartView.deselectAllDocuments")) {
			@Override
			protected void onSelectAll(ClickEvent event) {
				allSelected = true;
				presenter.setAllRecordsSelected(Document.SCHEMA_TYPE);
				for (CheckBox checkBox : allDocumentsCheckBoxes) {
					checkBox.setValue(true);
				}
				allSelected = false;
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				allSelected = false;
				for (CheckBox checkBox : allDocumentsCheckBoxes) {
					checkBox.setValue(false);
				}
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void buttonClickCallBack(boolean selectAllMode) {
				;
			}
		};

		documentSelectDeselectAllButton.addStyleName(ValoTheme.BUTTON_LINK);
		filterAndMenuBarComponent.setSpacing(true);
		documentFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.documentFilterButtonClicked();
			}
		};

		documentActionMenu = buildActionMenuDisplay();
		filterAndMenuBarComponent.addComponents(documentFilterField, filterButton, documentSelectDeselectAllButton, documentActionMenu);
		return filterAndMenuBarComponent;
	}

	private HorizontalLayout buildContainerFilterAndMenuBarComponent() {
		HorizontalLayout filterAndMenuBarComponent = new HorizontalLayout();
		containerSelectDeselectAllButton = new SelectDeselectAllButton($("CartView.selectAllContainers"), $("CartView.deselectAllContainers")) {
			@Override
			protected void onSelectAll(ClickEvent event) {
				allSelected = true;
				presenter.setAllRecordsSelected(ContainerRecord.SCHEMA_TYPE);
				for (CheckBox checkBox : allContainersCheckBoxes) {
					checkBox.setValue(true);
				}
				allSelected = false;
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void onDeselectAll(ClickEvent event) {
				allSelected = false;
				for (CheckBox checkBox : allContainersCheckBoxes) {
					checkBox.setValue(false);
				}
				refreshSelectionActionMenuBar();
			}

			@Override
			protected void buttonClickCallBack(boolean selectAllMode) {
				;
			}
		};

		containerSelectDeselectAllButton.addStyleName(ValoTheme.BUTTON_LINK);
		filterAndMenuBarComponent.setSpacing(true);
		containerFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.containerFilterButtonClicked();
			}
		};
		containerActionMenu = buildActionMenuDisplay();
		filterAndMenuBarComponent.addComponents(containerFilterField, filterButton, containerSelectDeselectAllButton, containerActionMenu);
		return filterAndMenuBarComponent;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		addStyleName("cart-view");

		FireableTabSheet tabSheet = new FireableTabSheet();
		folderTable = buildFolderTable("CartView.folders", presenter.getFolderRecords());
		documentTable = buildTable("CartView.documents", presenter.getDocumentRecords(), FavorisType.FAVORITE_DOCUMENTS);
		containerTable = buildTable("CartView.containers", presenter.getContainerRecords(), FavorisType.FAVORITE_CONTAINER);

		TabSheet.Tab folderTab = tabSheet.addTab(folderLayout = new CartTabLayout(buildFolderFilterAndMenuBarComponent(), folderTable));
		folderTab.setCaption($("CartView.foldersTab"));
		folderLayout.setSchemaType(Folder.SCHEMA_TYPE);
		folderTab.setVisible(!folderTable.getContainerDataSource().getItemIds().isEmpty());

		TabSheet.Tab documentTab = tabSheet.addTab(documentLayout = new CartTabLayout(buildDocumentFilterAndMenuBarComponent(), documentTable));
		documentTab.setCaption($("CartView.documentsTab"));
		documentLayout.setSchemaType(Document.SCHEMA_TYPE);
		documentTab.setVisible(!documentTable.getContainerDataSource().getItemIds().isEmpty());

		TabSheet.Tab containerTab = tabSheet.addTab(containerLayout = new CartTabLayout(buildContainerFilterAndMenuBarComponent(), containerTable));
		containerTab.setCaption($("CartView.containersTab"));
		containerLayout.setSchemaType(ContainerRecord.SCHEMA_TYPE);
		containerTab.setVisible(!containerTable.getContainerDataSource().getItemIds().isEmpty());

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Component selectedTab = event.getTabSheet().getSelectedTab();
				if (selectedTab instanceof CartTabLayout) {
					currentSchemaType = ((CartTabLayout) selectedTab).getSchemaType();
					presenter.emptySelectedRecords();
					refreshSelectionActionMenuBar();
				}
			}
		});
		if (!folderTab.isVisible() && !documentTab.isVisible() && !containerTab.isVisible()) {
			mainLayout.addComponent(new Label($("CartView.emptyCart")));
		} else {
			mainLayout.addComponent(tabSheet);
			tabSheet.fireTabSelectionChanged();
		}
		return mainLayout;
	}

	private ActionMenuDisplay buildActionMenuDisplay() {
		ActionMenuDisplay actionMenuDisplay = new ActionMenuDisplay(presenter::getMenuItemActionsForBatch, presenter::getMenuItemRecordProvider, presenter::getActionMenuDisplaySchemaTypecodeForBatch) {

			@NotNull
			@Override
			public String getMenuBarRootCaption() {
				return $("ConstellioHeader.selectionActions");
			}

			@Override
			public void actionExecuted(MenuItemAction menuItemAction, Object component) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();

				if (currentView.getClass().equals(CartViewImpl.this.getClass())) {
					refresh();
				}
			}

			@Override
			public boolean isQuickActionsAreVisible() {
				return false;
			}

			@Override
			public boolean isHiddenWhenNoActionOrStateAreAllNotVisible() {
				return true;
			}
		};

		actionMenuDisplay.setVisible(true);

		return actionMenuDisplay;
	}

	private Table buildFolderTable(final String tableId, final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		final Container container = buildFolderContainer(dataProvider);
		Table table = new RecordVOTable($("CartView.records", container.size()), container) {
			@Override
			protected String getTableId() {
				return tableId;
			}

			@Override
			protected TableColumnsManager newColumnsManager() {
				return new TableColumnsManager();
			}

			@Override
			protected Property<?> loadContainerProperty(Object itemId, Object propertyId) {
				Property loadContainerProperty = null;
				if (itemId instanceof Integer && CommonMetadataBuilder.SUMMARY.equals(propertyId)) {
					RecordVO recordVO = dataProvider.getRecordVO((int) itemId);
					if (recordVO.getMetadataOrNull(recordVO.getSchema().getCode() + "_" + Folder.SUMMARY) != null) {
						MetadataVO metadataVO = recordVO.getSchema().getMetadata(Folder.SUMMARY);
						String value = recordVO.get(recordVO.getSchema().getMetadata(Folder.SUMMARY));
						if (metadataVO != null && !Strings.isNullOrEmpty(value)) {
							loadContainerProperty = new ObjectProperty(value, Component.class);
						}
					}
				} else {
					loadContainerProperty = super.loadContainerProperty(itemId, propertyId);
					if (loadContainerProperty.getValue() instanceof String) {
						String value = (String) loadContainerProperty.getValue();
						if (Strings.isNullOrEmpty(value)) {
							loadContainerProperty = super.loadContainerProperty(itemId, Schemas.TITLE.getLocalCode());
						}
					}
				}

				return loadContainerProperty;
			}

		};
		table.addItemClickListener((ItemClickListener) event -> {
			RecordVOWithDistinctSchemaItem item = (RecordVOWithDistinctSchemaItem) event.getItem();
			presenter.displayRecordRequested(item.getBean());
		});
		table.setColumnHeader(CommonMetadataBuilder.SUMMARY, $("CartViewImpl.summary"));
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();

		SelectionCheckBoxGenerator selectionCheckBoxGenerator = new SelectionCheckBoxGenerator(dataProvider, allFoldersCheckBoxes);
		table = selectionCheckBoxGenerator.attachTo(table);

		return table;
	}

	private Table buildTable(final String tableId, final RecordVOWithDistinctSchemasDataProvider dataProvider,
							 FavorisType type) {
		final Container container = buildContainer(dataProvider);
		Table table = new RecordVOTable($("CartView.records", container.size()), container) {
			@Override
			protected String getTableId() {
				return tableId;
			}

			@Override
			protected TableColumnsManager newColumnsManager() {
				return new TableColumnsManager();
			}

			@Override
			protected Property<?> loadContainerProperty(Object itemId, Object propertyId) {
				Property loadContainerProperty = super.loadContainerProperty(itemId, propertyId);
				if (loadContainerProperty.getValue() instanceof String) {
					String value = (String) loadContainerProperty.getValue();
					if (Strings.isNullOrEmpty(value)) {
						loadContainerProperty = super.loadContainerProperty(itemId, Schemas.TITLE.getLocalCode());
					}
				}

				return loadContainerProperty;
			}

		};
		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOWithDistinctSchemaItem item = (RecordVOWithDistinctSchemaItem) event.getItem();
				presenter.displayRecordRequested(item.getBean());
			}
		});
		table.setColumnHeader(CommonMetadataBuilder.SUMMARY, $("CartViewImpl.summary"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("title"));
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();

		SelectionCheckBoxGenerator selectionCheckBoxGenerator = new SelectionCheckBoxGenerator(dataProvider, getCheckBoxes(type));
		table = selectionCheckBoxGenerator.attachTo(table);
		return table;
	}

	private List<CheckBox> getCheckBoxes(FavorisType type) {
		switch (type) {
			case FAVORITE_FOLDERS:
				return allFoldersCheckBoxes;
			case FAVORITE_CONTAINER:
				return allContainersCheckBoxes;
			case FAVORITE_DOCUMENTS:
				return allDocumentsCheckBoxes;
			default:
				LOGGER.error("Missing case in enum com.constellio.app.modules.rm.ui.pages.cart.CartViewImpl.FavorisType");
				return new ArrayList<>();
		}
	}

	@Override
	public void startDownload(final InputStream stream, String filename) {
		Resource resource = new DownloadStreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, filename);
		Page.getCurrent().open(resource, null, false);
	}

	@Override
	public void filterFolderTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if (folderFilterField.isEmpty()) {
			dataProvider = presenter.getFolderRecords();
		} else {
			dataProvider = presenter.getFilteredFolderRecords(folderFilterField.getValue());
		}
		Table newTable = buildTable("CartView.folders", dataProvider, FavorisType.FAVORITE_FOLDERS);
		folderLayout.replaceComponent(folderTable, newTable);
		folderTable = newTable;
		presenter.emptySelectedRecords();
		refreshSelectionActionMenuBar();
	}

	@Override
	public void filterDocumentTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if (documentFilterField.isEmpty()) {
			dataProvider = presenter.getDocumentRecords();
		} else {
			dataProvider = presenter.getFilteredDocumentRecords(documentFilterField.getValue());
		}
		Table newTable = buildTable("CartView.documents", dataProvider, FavorisType.FAVORITE_DOCUMENTS);
		documentLayout.replaceComponent(documentTable, newTable);
		documentTable = newTable;
		presenter.emptySelectedRecords();
		refreshSelectionActionMenuBar();
	}

	@Override
	public void filterContainerTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if (containerFilterField.isEmpty()) {
			dataProvider = presenter.getContainerRecords();
		} else {
			dataProvider = presenter.getFilteredContainerRecords(containerFilterField.getValue());
		}
		Table newTable = buildTable("CartView.containers", dataProvider, FavorisType.FAVORITE_CONTAINER);
		containerLayout.replaceComponent(containerTable, newTable);
		containerTable = newTable;
		presenter.emptySelectedRecords();
		refreshSelectionActionMenuBar();
	}

	private Container buildContainer(final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		RecordVOWithDistinctSchemaTypesLazyContainer records = new RecordVOWithDistinctSchemaTypesLazyContainer(
				dataProvider, asList(CommonMetadataBuilder.TITLE));
		ButtonsContainer<RecordVOWithDistinctSchemaTypesLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						int index = (int) itemId;
						presenter.itemRemovalRequested(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		return container;
	}

	private Container buildFolderContainer(final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		RecordVOWithDistinctSchemaTypesLazyContainer records = new RecordVOWithDistinctSchemaTypesLazyContainer(
				dataProvider, asList(CommonMetadataBuilder.TITLE, CommonMetadataBuilder.SUMMARY));
		ButtonsContainer<RecordVOWithDistinctSchemaTypesLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						int index = (int) itemId;
						presenter.itemRemovalRequested(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		return container;
	}

	@Override
	public String getCurrentSchemaType() {
		return currentSchemaType;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	private class FireableTabSheet extends TabSheet {
		public void fireTabSelectionChanged() {
			fireSelectedTabChange();
		}
	}

	private class CartTabLayout extends VerticalLayout {
		private String schemaType = null;

		public CartTabLayout() {
			super();
		}

		public CartTabLayout(Component... children) {
			super(children);
		}

		public CartTabLayout setSchemaType(String schemaType) {
			this.schemaType = schemaType;
			return this;
		}

		public String getSchemaType() {
			return schemaType;
		}
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return !isNested;
	}

	public void refreshSelectionActionMenuBar() {
		folderActionMenu.refresh();
		documentActionMenu.refresh();
		containerActionMenu.refresh();
	}

	private class SelectionCheckBoxGenerator implements ColumnGenerator {
		public static final String CHECKBOX = "checkbox";

		private RecordVOWithDistinctSchemasDataProvider dataProvider;
		private List<CheckBox> checkBoxesRef;

		public SelectionCheckBoxGenerator(RecordVOWithDistinctSchemasDataProvider dataProvider,
										  List<CheckBox> checkBoxesRef) {
			this.dataProvider = dataProvider;
			this.checkBoxesRef = checkBoxesRef;
			checkBoxesRef.clear();
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (CHECKBOX.equals(columnId)) {
				if (itemId instanceof Integer) {
					RecordVO recordVO = dataProvider.getRecordVO((int) itemId);
					return buildCheckBox(recordVO);
				}
			}
			return null;
		}

		public Table attachTo(Table table) {
			List<Object> visibleColumns = new ArrayList<>(Arrays.asList(table.getVisibleColumns()));

			table.addGeneratedColumn(CHECKBOX, this);
			table.setColumnHeader(CHECKBOX, "");
			table.setColumnAlignment(CHECKBOX, Align.CENTER);
			table.setColumnWidth(CHECKBOX, 50);

			visibleColumns.add(0, SelectionCheckBoxGenerator.CHECKBOX);
			table.setVisibleColumns(visibleColumns.toArray());

			return table;
		}

		private Object buildCheckBox(RecordVO recordVO) {
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(presenter.getSelectedRecords().stream().anyMatch(record -> record.getId().equals(recordVO.getId())));

			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {

					if(!allSelected) {
						if (checkBox.getValue()) {
							presenter.addToSelectedRecords(recordVO.getId());
							refreshSelectionActionMenuBar();
						} else {
							presenter.removeFromSelectedRecords(recordVO.getId());
							refreshSelectionActionMenuBar();
						}
					}
				}
			});

			checkBoxesRef.add(checkBox);
			return checkBox;
		}
	}

	private enum FavorisType {
		FAVORITE_CONTAINER, FAVORITE_DOCUMENTS, FAVORITE_FOLDERS
	}
}
