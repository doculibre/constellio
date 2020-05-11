package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory;
import com.constellio.app.services.menu.MenuItemFactory.CommandCallback;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
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
import org.apache.commons.collections4.MapUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class CartViewImpl extends BaseViewImpl implements CartView {

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
	private RecordListMenuBar folderListMenuBar;
	private RecordListMenuBar documentListMenuBar;
	private RecordListMenuBar containerListMenuBar;

	public CartViewImpl() {
		this(null, false);
	}

	public CartViewImpl(String cartId, boolean isNested) {
		this.isNested = isNested;
		presenter = new CartPresenter(cartId, this);
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
	protected boolean isActionMenuBar() {
		return true;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		Cart cart = presenter.getCart();
		Record record = null;
		if (cart != null) {
			record = cart.getWrappedRecord();
		} else {
			record = new RecordImpl(presenter.getCartMetadataSchema(), presenter.getCurrentUser().getId());
		}

		List<String> excludedActionTypes = Arrays.asList(RMRecordsMenuItemActionType.RMRECORDS_ADD_CART.name());
		List<MenuItemAction> menuItemActions = new MenuItemServices(this.getCollection(), getConstellioFactories().getAppLayerFactory())
				.getActionsForRecord(record, excludedActionTypes, new MenuItemActionBehaviorParams() {
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
				});
		return new MenuItemFactory().buildActionButtons(menuItemActions, new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return presenter.getAllCartItemRecords();
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		}, new CommandCallback() {
			@Override
			public void actionExecuted(MenuItemAction menuItemAction, Object component) {
				Button button = (Button) component;
				button.setEnabled(menuItemAction.getState().getStatus() != MenuItemActionStateStatus.DISABLED);
				button.setEnabled(menuItemAction.getState().getStatus() == MenuItemActionStateStatus.VISIBLE);
				//				View currentView = ConstellioUI.getCurrent().getCurrentView();
				//				// No point in refreshing menu if we left the original page
				//				if (currentView == originalView) {
				//					// Recursive call
				//					buildMenuItems();
				//				}
			}

		});
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

	private HorizontalLayout buildFolderFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		folderFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.folderFilterButtonClicked();
			}
		};
		filterComponent.addComponents(folderFilterField, filterButton);
		return filterComponent;
	}

	private HorizontalLayout buildDocumentFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		documentFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.documentFilterButtonClicked();
			}
		};
		filterComponent.addComponents(documentFilterField, filterButton);
		return filterComponent;
	}

	private HorizontalLayout buildContainerFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		containerFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.containerFilterButtonClicked();
			}
		};
		filterComponent.addComponents(containerFilterField, filterButton);
		return filterComponent;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		FireableTabSheet tabSheet = new FireableTabSheet();
		folderTable = buildFolderTable("CartView.folders", presenter.getFolderRecords());
		documentTable = buildTable("CartView.documents", presenter.getDocumentRecords());
		containerTable = buildTable("CartView.containers", presenter.getContainerRecords());
		folderListMenuBar = buildSelectionPanelMenuBar();
		folderListMenuBar.setVisible(true);
		folderListMenuBar.buildMenuItems();
		TabSheet.Tab folderTab = tabSheet.addTab(folderLayout = new CartTabLayout(buildFolderFilterComponent(), folderListMenuBar, folderTable));
		folderTab.setCaption($("CartView.foldersTab"));
		folderLayout.setSchemaType(Folder.SCHEMA_TYPE);
		folderTab.setVisible(!folderTable.getContainerDataSource().getItemIds().isEmpty());
		documentListMenuBar = buildSelectionPanelMenuBar();
		documentListMenuBar.setVisible(true);
		documentListMenuBar.buildMenuItems();
		TabSheet.Tab documentTab = tabSheet.addTab(documentLayout = new CartTabLayout(buildDocumentFilterComponent(), documentListMenuBar, documentTable));
		documentTab.setCaption($("CartView.documentsTab"));
		documentLayout.setSchemaType(Document.SCHEMA_TYPE);
		documentTab.setVisible(!documentTable.getContainerDataSource().getItemIds().isEmpty());
		containerListMenuBar = buildSelectionPanelMenuBar();
		containerListMenuBar.setVisible(true);
		containerListMenuBar.buildMenuItems();
		TabSheet.Tab containerTab = tabSheet.addTab(containerLayout = new CartTabLayout(buildContainerFilterComponent(), containerListMenuBar, containerTable));
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
		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				int itemId = (int) event.getItemId();
				presenter.displayRecordRequested(dataProvider.getRecordVO(itemId));
			}
		});
		table.setColumnHeader(CommonMetadataBuilder.SUMMARY, $("CartViewImpl.summary"));
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();

		SelectionCheckBoxGenerator selectionCheckBoxGenerator = new SelectionCheckBoxGenerator(dataProvider);
		table = selectionCheckBoxGenerator.attachTo(table);

		return table;
	}

	private Table buildTable(final String tableId, final RecordVOWithDistinctSchemasDataProvider dataProvider) {
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
				int itemId = (int) event.getItemId();
				presenter.displayRecordRequested(dataProvider.getRecordVO(itemId));
			}
		});
		table.setColumnHeader(CommonMetadataBuilder.SUMMARY, $("CartViewImpl.title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("title"));
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();
		SelectionCheckBoxGenerator selectionCheckBoxGenerator = new SelectionCheckBoxGenerator(dataProvider);
		table = selectionCheckBoxGenerator.attachTo(table);
		return table;
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
		Table newTable = buildTable("CartView.folders", dataProvider);
		folderLayout.replaceComponent(folderTable, newTable);
		folderTable = newTable;
	}

	@Override
	public void filterDocumentTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if (documentFilterField.isEmpty()) {
			dataProvider = presenter.getDocumentRecords();
		} else {
			dataProvider = presenter.getFilteredDocumentRecords(documentFilterField.getValue());
		}
		Table newTable = buildTable("CartView.documents", dataProvider);
		documentLayout.replaceComponent(documentTable, newTable);
		documentTable = newTable;
	}

	@Override
	public void filterContainerTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if (containerFilterField.isEmpty()) {
			dataProvider = presenter.getContainerRecords();
		} else {
			dataProvider = presenter.getFilteredContainerRecords(containerFilterField.getValue());
		}
		Table newTable = buildTable("CartView.containers", dataProvider);
		containerLayout.replaceComponent(containerTable, newTable);
		containerTable = newTable;
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
		folderListMenuBar.buildMenuItems();
		documentListMenuBar.buildMenuItems();
		containerListMenuBar.buildMenuItems();
	}

	private class SelectionCheckBoxGenerator implements ColumnGenerator {
		public static final String CHECKBOX = "checkbox";

		private RecordVOWithDistinctSchemasDataProvider dataProvider;

		public SelectionCheckBoxGenerator(RecordVOWithDistinctSchemasDataProvider dataProvider) {
			this.dataProvider = dataProvider;
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
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if (checkBox.getValue()) {
						presenter.addToSelectedRecords(recordVO.getId());
						refreshSelectionActionMenuBar();
					} else {
						presenter.removeFromSelectedRecords(recordVO.getId());
						refreshSelectionActionMenuBar();
					}
				}
			});

			return checkBox;
		}
	}

}
