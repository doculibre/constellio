package com.constellio.app.ui.pages.home;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.entities.navigation.PageItem.CustomItem;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable.RecentItem;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.entities.navigation.PageItem.SharedItemsTables;
import com.constellio.app.modules.rm.ui.components.tree.RMTreeDropHandlerImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.components.PlaceHolder;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet;
import com.constellio.app.ui.framework.components.tree.TreeItemClickListener;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.decorators.contextmenu.ContextMenuDecorator;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree.TreeDragMode;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class HomeViewImpl extends BaseViewImpl implements HomeView, PartialRefresh {

	private final HomePresenter presenter;
	private List<PageItem> tabs;
	private TabSheet tabSheet;

	private List<ContextMenuDecorator> contextMenuDecorators = new ArrayList<>();

	public HomeViewImpl() {
		presenter = new HomePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		tabs = presenter.forParams(event.getParameters()).getTabs();
	}

	@Override
	protected String getTitle() {
		return $("HomeView.viewTitle");
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		tabSheet = new TabSheet();
		tabSheet.addStyleName("records-management");

		Map<String, Tab> tabsByCode = new HashMap<>();
		for (PageItem item : tabs) {
			Tab tab = tabSheet.addTab(new PlaceHolder(), $("HomeView.tab." + item.getCode()));
			tab.setVisible(isTabVisible(tab));
			tabsByCode.put(item.getCode(), tab);
		}

		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Tab currentTab = tabSheet.getTab(tabSheet.getSelectedTab());
				selectTab(currentTab);
			}
		});

		selectTab(tabsByCode.get(presenter.getCurrentTab()));

		return tabSheet;
	}

	public boolean isTabVisible(Tab tab) {
		int indexOfSelectedTab = tabSheet.getTabPosition(tab);
		PageItem tabSource = tabs.get(indexOfSelectedTab);

		if (tabSource instanceof CustomItem) {
			return presenter.isCustomItemVisible((CustomItem) tabSource);
		} else {
			return true;
		}
	}

	@Override
	public String getSelectedTabCode() {
		return presenter.getCurrentTab();
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}


		int position = tabSheet.getTabPosition(tab);
		PageItem item = tabs.get(position);

		presenter.tabSelected(item.getCode());
		tabSheet.setSelectedTab(position);

		PlaceHolder tabComponent = (PlaceHolder) tab.getComponent();

		if (presenter.isRefreshable(item.getCode())) {
			tabComponent.setCompositionRoot(null);
		}

		if (tabComponent.getComponentCount() == 0) {
			tabComponent.setCompositionRoot(buildComponentFor(tab));
		}

		ParamUtils.setParams(item.getCode());
	}

	private Component buildComponentFor(Tab tab) {
		int indexOfSelectedTab = tabSheet.getTabPosition(tab);
		PageItem tabSource = tabs.get(indexOfSelectedTab);
		switch (tabSource.getType()) {
			case RECENT_ITEM_TABLE:
				return buildRecentItemTable((RecentItemTable) tabSource);
			case RECORD_TABLE:
				return buildRecordTable((RecordTable) tabSource);
			case RECORD_TREE:
				return buildRecordTreeOrRecordMultiTree((RecordTree) tabSource);
			case SHARED_ITEMS_TABLES:
				return buildSharedTabs((SharedItemsTables) tabSource);
			case CUSTOM_ITEM:
				return buildCustomComponent((CustomItem) tabSource);
			default:
				throw new RuntimeException("Unsupported tab type : " + tabSource.getType());
		}
	}

	private Component getSelectedTabComponent() {
		Component selectedTabComponent;

		PlaceHolder placeHolder = (PlaceHolder) tabSheet.getSelectedTab();
		Component compositionRoot = placeHolder.getCompositionRoot();
		if (compositionRoot != null) {
			if (compositionRoot instanceof TabSheet) {
				TabSheet subTabSheet = (TabSheet) compositionRoot;
				Component subTabSheetSelectedTab = subTabSheet.getSelectedTab();
				if (subTabSheetSelectedTab instanceof PlaceHolder) {
					PlaceHolder subTabSheetSelectedTabPlaceHolder = (PlaceHolder) subTabSheet.getSelectedTab();
					selectedTabComponent = subTabSheetSelectedTabPlaceHolder.getCompositionRoot();
				} else if (subTabSheetSelectedTab instanceof RecordLazyTreeTabSheet.PlaceHolder) {
					RecordLazyTreeTabSheet.PlaceHolder subTabSheetSelectedTabPlaceHolder =
							(RecordLazyTreeTabSheet.PlaceHolder) subTabSheet.getSelectedTab();
					selectedTabComponent = subTabSheetSelectedTabPlaceHolder.getCompositionRoot();
				} else {
					selectedTabComponent = subTabSheetSelectedTab;
				}
			} else {
				selectedTabComponent = compositionRoot;
			}
		} else {
			selectedTabComponent = null;
		}

		return selectedTabComponent;
	}

	private Component buildRecentItemTable(RecentItemTable tabSource) {
		String tableId = "HomeView." + tabSource.getCode();
		String schemaTypeCode = tabSource.getSchemaType();
		List<RecentItem> recentItems = tabSource.getItems(getConstellioFactories().getAppLayerFactory(), getSessionContext());
		return new ViewableRecentItemTablePanel(schemaTypeCode, tableId, recentItems);
	}

	private Component buildSharedTabs(final SharedItemsTables sharedItemsTables) {

		Map<String, RecordVODataProvider> dataProviders = sharedItemsTables
				.getDataProvider(getConstellioFactories().getAppLayerFactory(), getSessionContext());
		TabSheet tabs = new TabSheet();
		for (Entry<String, RecordVODataProvider> dataProvider : dataProviders.entrySet()) {
			tabs.addTab(buildTable(dataProvider.getValue()), $(dataProvider.getKey()));
		}
		return tabs;
	}

	private Component buildRecordTable(final RecordTable recordTable) {
		RecordVODataProvider dataProvider = recordTable
				.getDataProvider(getConstellioFactories().getAppLayerFactory(), getSessionContext());
		return buildTable(dataProvider);
	}


	private Component buildTable(RecordVODataProvider dataProvider) {
		final ViewableRecordVOTablePanel table = new ViewableRecordItemTablePanel(dataProvider);
		table.addStyleName("record-table");
		table.setSizeFull();
		table.setAllItemsVisible(true);
		return table;
	}

	private Component buildRecordTreeOrRecordMultiTree(RecordTree recordTree) {
		List<RecordLazyTreeDataProvider> providers = recordTree.getDataProviders(
				getConstellioFactories().getAppLayerFactory(), getSessionContext());
		return providers.size() > 1 ?
			   buildRecordMultiTree(recordTree, providers) :
			   buildRecordTree(recordTree, providers.get(0));
	}

	private RecordLazyTreeTabSheet buildRecordMultiTree(final RecordTree recordTree,
														List<RecordLazyTreeDataProvider> providers) {
		final RecordLazyTreeTabSheet subTabSheet = new RecordLazyTreeTabSheet(providers) {
			@Override
			protected RecordLazyTree newLazyTree(RecordLazyTreeDataProvider dataProvider, int bufferSize) {
				return buildRecordTree(recordTree, dataProvider);
			}
		};
		subTabSheet.addStyleName("tabsheet-secondary");
		subTabSheet.setSelectedTab(recordTree.getDefaultDataProvider());
		return subTabSheet;
	}

	private static int getBufferSizeFromConfig() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		return modelLayerFactory.getSystemConfigs().getLazyTreeBufferSize();
	}

	private RecordLazyTree buildRecordTree(RecordTree recordTree, final RecordLazyTreeDataProvider provider) {
		RecordLazyTree tree = new RecordLazyTree(provider, getBufferSizeFromConfig());
		tree.addItemClickListener(new TreeItemClickListener() {

			boolean clickNavigating;

			@Override
			public boolean shouldExpandOrCollapse(ItemClickEvent event) {
				return !clickNavigating;
			}

			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					String recordId = (String) event.getItemId();
					clickNavigating = presenter.recordClicked(recordId, provider.getTaxonomyCode(), false);
				} else {
					clickNavigating = true;
				}
			}
		});
		ContextMenu menu = recordTree.getContextMenu();
		for (ContextMenuDecorator contextMenuDecorator : contextMenuDecorators) {
			menu = contextMenuDecorator.decorate(this, menu);
		}
		if (menu != null) {
			tree.setContextMenu(menu);
		}

		if (ResponsiveUtils.isDesktop()) {
			tree.setDragMode(TreeDragMode.NODE);
		} else {
			tree.setDragMode(TreeDragMode.NONE);
		}

		tree.setDropHandler(new RMTreeDropHandlerImpl() {
			@Override
			public void showErrorMessage(String errorMessage) {
				HomeViewImpl.this.showErrorMessage(errorMessage);
			}
		});

		tree.loadAndExpand(recordTree.getExpandedRecordIds());

		return tree;
	}

	private Component buildCustomComponent(CustomItem tabSource) {
		ItemClickListener itemClickListener = new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					RecordVOItem recordItem = (RecordVOItem) event.getItem();
					RecordVO recordVO = recordItem.getRecord();
					presenter.recordClicked(recordVO.getId(), null, false);
				}
			}
		};
		Component component = tabSource.buildCustomComponent(getConstellioFactories(), getSessionContext(), itemClickListener);
		if (component instanceof BaseViewImpl) {
			((BaseViewImpl) component).enter(null);
		}
		component.setSizeFull();
		return component;
	}

	public void addContextMenuDecorator(ContextMenuDecorator decorator) {
		this.contextMenuDecorators.add(decorator);
	}

	public List<ContextMenuDecorator> getContextMenuDecorators() {
		return this.contextMenuDecorators;
	}

	public void removeContextMenuDecorator(ContextMenuDecorator decorator) {
		this.contextMenuDecorators.remove(decorator);
	}

	@Override
	public void recordChanged(String recordId) {
		presenter.recordChanged(recordId);
	}

	public void updateCaption(String recordId, String newCaption) {
		Component selectedTabComponent = getSelectedTabComponent();
		if (selectedTabComponent instanceof RecordLazyTree) {
			RecordLazyTree recordLazyTree = (RecordLazyTree) selectedTabComponent;
			recordLazyTree.setItemCaption(recordId, newCaption);
		}
	}

	@Override
	public void doPartialRefresh() {
		if (presenter.isRefreshable(getSelectedTabCode())) {
			Component component = tabSheet.getSelectedTab();

			tabSheet.getTab(tabSheet.getSelectedTab());

			if (component instanceof PlaceHolder) {
				PlaceHolder placeHolder = (PlaceHolder) component;
				placeHolder.setHeightUndefined();

				Component compositionRoot = placeHolder.getCompositionRoot();
				compositionRoot.setHeightUndefined();
				if (compositionRoot instanceof ViewableRecordItemTablePanel) {
					ViewableRecordItemTablePanel tablePanel = (ViewableRecordItemTablePanel) compositionRoot;

					tablePanel.setHeightUndefined();

					RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) tablePanel.getRecordVOContainer();
					recordVOLazyContainer.forceRefresh();
				}
			}
		}
	}

	private class RecentItemContainer extends BeanItemContainer<RecentItem> implements RecordVOContainer {

		private MetadataSchemaVO schemaVO;

		public RecentItemContainer(String schemaTypeCode, String tableId, List<RecentItem> recentItems)
				throws IllegalArgumentException {
			super(RecentItem.class, recentItems);
			String collection = getSessionContext().getCurrentCollection();
			MetadataSchemaType schemaType = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(schemaTypeCode);
			schemaVO = new MetadataSchemaToVOBuilder().build(schemaType.getDefaultSchema(), VIEW_MODE.TABLE, getSessionContext());
		}

		@Override
		public Collection<String> getContainerPropertyIds() {
			return Arrays.asList(Schemas.TITLE_CODE, RecentItem.LAST_ACCESS);
		}

		@Override
		public Class<?> getType(Object propertyId) {
			if (RecentItem.LAST_ACCESS.equals(propertyId)) {
				return String.class;
			} else if (Schemas.TITLE_CODE.equals(propertyId) || (propertyId instanceof MetadataVO && ((MetadataVO) propertyId).codeMatches(Schemas.TITLE_CODE))) {
				return String.class;
			}
			return super.getType(propertyId);
		}

		@Override
		public Property<?> getContainerProperty(Object itemId, Object propertyId) {
			if (RecentItem.LAST_ACCESS.equals(propertyId)) {
				RecentItem recentItem = (RecentItem) itemId;
				String value = new JodaDateTimeToStringConverter()
						.convertToPresentation(recentItem.getLastAccess(), String.class, getSessionContext().getCurrentLocale());
				return new ObjectProperty<>(value);
			} else if (Schemas.TITLE_CODE.equals(propertyId) || (propertyId instanceof MetadataVO && ((MetadataVO) propertyId).codeMatches(Schemas.TITLE_CODE))) {
				RecentItem recentItem = (RecentItem) itemId;
				String value = recentItem.getTitle();
				return new ObjectProperty<>(value);
			}
			return super.getContainerProperty(itemId, propertyId);
		}

		@Override
		public void forceRefresh() {
		}

		@Override
		public RecordVO getRecordVO(Object itemId) {
			BeanItem<RecentItem> recordVOItem = (BeanItem<RecentItem>) getItem(itemId);
			return recordVOItem.getBean().getRecord();
		}

		@Override
		public List<MetadataSchemaVO> getSchemas() {
			return Arrays.asList(schemaVO);
		}

		@Override
		public Map<String, List<String>> getHighlights(Object itemId) {
			return new HashMap<>();
		}

	}

	private class ViewableRecordItemTablePanel extends HomeViewViewableItemTablePanel {

		public ViewableRecordItemTablePanel(RecordVODataProvider dataProvider) {
			super(new RecordVOLazyContainer(dataProvider) {
			});

			addItemClickListener(new ItemClickListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.getButton() == MouseButton.LEFT) {
						RecordVOItem item = (RecordVOItem) event.getItem();
						presenter.recordClicked(item.getRecord().getId(), null, true);
					}
				}
			});
			setVisibleColumns();
			setSelectionActionButtons();
		}
	}

	private class ViewableRecentItemTablePanel extends HomeViewViewableItemTablePanel {

		public ViewableRecentItemTablePanel(String schemaTypeCode, String tableId, List<RecentItem> recentItems) {
			super(new RecentItemContainer(schemaTypeCode, tableId, recentItems));

			//			MetadataSchemaVO schemaVO = getSchemas().get(0);
			//			MetadataVO titleMetadata = schemaVO.getMetadata(Schemas.TITLE_CODE);

			//			addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
			//
			//			setVisibleColumns(titleMetadata, RecentItem.LAST_ACCESS);
			setColumnHeader(Schemas.TITLE_CODE, $("HomeView.recentItem.caption"));
			setColumnHeader(RecentItem.LAST_ACCESS, $("HomeView.recentItem.lastAccess"));
			setColumnExpandRatio(Schemas.TITLE_CODE, 1);

			addItemClickListener(new ItemClickListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.getButton() == MouseButton.LEFT) {
						BeanItem<RecentItem> item = (BeanItem<RecentItem>) event.getItem();
						presenter.recordClicked(item.getBean().getId(), null, true);
					}
				}
			});
			setSelectionActionButtons();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected RecordVO getRecordVOForTitleColumn(Item item) {
			BeanItem<RecentItem> recordVOItem = (BeanItem<RecentItem>) item;
			return recordVOItem.getBean().getRecord();
		}

	}

	private class HomeViewViewableItemTablePanel extends ViewableRecordVOTablePanel {
		protected Set<Object> selectedItemIds;

		public HomeViewViewableItemTablePanel(RecordVOContainer container) {
			super(container);
			setAllItemsVisible(true);
		}

		public void initSelectedItemCache() {
			if (selectedItemIds == null) {
				selectedItemIds = new HashSet<>();
			}
		}

		@Override
		protected boolean isSelectColumn() {
			return true;
		}

		@Override
		protected SelectionManager newSelectionManager() {
			return new SelectionManager() {
				@Override
				public void selectionChanged(SelectionChangeEvent event) {
					initSelectedItemCache();

					List<Object> selectedItemIdsFromEvent = event.getSelectedItemIds();
					List<Object> deselectedItemIdsFromEvent = event.getDeselectedItemIds();

					if (deselectedItemIdsFromEvent != null && !deselectedItemIdsFromEvent.isEmpty()) {
						for (Object currentDeselectedItem : deselectedItemIdsFromEvent) {
							selectedItemIds.remove(currentDeselectedItem);
						}
					}

					if (selectedItemIdsFromEvent != null && !selectedItemIdsFromEvent.isEmpty()) {
						for (Object currentselectedItem : selectedItemIdsFromEvent) {
							selectedItemIds.add(currentselectedItem);
						}
					}


					boolean allItemsSelected = event.isAllItemsSelected();
					boolean allItemsDeselected = event.isAllItemsDeselected();
					if (allItemsSelected) {
						Collection<?> itemIds = getItemIds();

						selectedItemIds.addAll(itemIds);
					} else if (allItemsDeselected) {
						selectedItemIds.clear();
					}
				}

				@Override
				public List<Object> getAllSelectedItemIds() {
					initSelectedItemCache();
					return new ArrayList<>(selectedItemIds);
				}

				@Override
				public boolean isAllItemsSelected() {
					boolean allItemsSelected = true;
					for (Object itemId : getItemIds()) {
						if (!isSelected(itemId)) {
							allItemsSelected = false;
							break;
						}
					}
					return allItemsSelected;
				}

				@Override
				public boolean isAllItemsDeselected() {
					boolean allItemsDeselected = true;
					for (Object itemId : getItemIds()) {
						if (isSelected(itemId)) {
							allItemsDeselected = false;
							break;
						}
					}
					return allItemsDeselected;
				}

				@Override
				public boolean isSelected(Object itemId) {
					initSelectedItemCache();


					return selectedItemIds.contains(itemId);
				}

				protected Collection<?> getItemIds() {
					return getActualTable().getItemIds();
				}
			};
		}
	}
}
