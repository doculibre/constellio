package com.constellio.app.ui.pages.home;

import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.entities.navigation.PageItem.CustomItem;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable.RecentItem;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.modules.rm.ui.components.tree.RMTreeDropHandlerImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.menuBar.RecordMenuBarHandler;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOSelectionTableAdapter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet;
import com.constellio.app.ui.framework.components.tree.TreeItemClickListener;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.decorators.contextmenu.ContextMenuDecorator;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree.TreeDragMode;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class HomeViewImpl extends BaseViewImpl implements HomeView {

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

		if(tabSource instanceof CustomItem) {
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
			case CUSTOM_ITEM:
				return buildCustomComponent((CustomItem) tabSource);
			default:
				throw new RuntimeException("Unsupported tab type : " + tabSource.getType());
		}
	}

	private Component buildRecentItemTable(RecentItemTable recentItems) {
		String tableId = "HomeView." + recentItems.getCode();
		final RecentTable recentTable = new RecentTable(tableId,
				recentItems.getItems(getConstellioFactories().getAppLayerFactory(), getSessionContext()));
		recentTable.setSizeFull();
		recentTable.addStyleName("record-table");
		return new RecordVOSelectionTableAdapter(recentTable) {
			@Override
			public void selectAll() {
				selectAllByItemId();
			}

			@Override
			public void deselectAll() {
				deselectAllByItemId();
			}

			@Override
			public boolean isAllItemsSelected() {
				return isAllItemsSelectedByItemId();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return isAllItemsDeselectedByItemId();
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				RecordVO recordVO = recentTable.getRecordVO(itemId);
				String recordId = recordVO.getId();
				presenter.selectionChanged(recordId, selected);
				adjustSelectAllButton(selected);
			}

			@Override
			public boolean isSelected(Object itemId) {
				RecordVO recordVO = recentTable.getRecordVO(itemId);
				String recordId = recordVO.getId();
				return presenter.isSelected(recordId);
			}
		};
	}

	private Component buildRecordTable(final RecordTable recordTable) {
		RecordVODataProvider dataProvider = recordTable
				.getDataProvider(getConstellioFactories().getAppLayerFactory(), getSessionContext());
		return buildTable(dataProvider);
	}

	private Component buildTable(RecordVODataProvider dataProvider) {
		RecordVOLazyContainer container = new RecordVOLazyContainer(dataProvider);
		final RecordVOTable table = new RecordVOTable(container);
		table.addStyleName("record-table");
		table.setSizeFull();
		for (Object item : table.getContainerPropertyIds()) {
			if (item instanceof MetadataVO) {
				MetadataVO property = (MetadataVO) item;
				if (property.getCode() != null && property.getCode().contains(Schemas.MODIFIED_ON.getLocalCode())) {
					table.setColumnWidth(property, 180);
				}
			}
		}
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					RecordVOItem recordItem = (RecordVOItem) event.getItem();
					RecordVO recordVO = recordItem.getRecord();
					presenter.recordClicked(recordVO.getId(), null);
				}
			}
		});
		return new RecordVOSelectionTableAdapter(table) {
			@Override
			public void selectAll() {
				selectAllByItemId();
			}

			@Override
			public void deselectAll() {
				deselectAllByItemId();
			}

			@Override
			public boolean isAllItemsSelected() {
				return isAllItemsSelectedByItemId();
			}

			@Override
			public boolean isAllItemsDeselected() {
				return isAllItemsDeselectedByItemId();
			}

			@Override
			public boolean isSelected(Object itemId) {
				RecordVOItem item = (RecordVOItem) table.getItem(itemId);
				String recordId = item.getRecord().getId();
				return presenter.isSelected(recordId);
			}

			@Override
			public void setSelected(Object itemId, boolean selected) {
				RecordVOItem item = (RecordVOItem) table.getItem(itemId);
				String recordId = item.getRecord().getId();
				presenter.selectionChanged(recordId, selected);
				adjustSelectAllButton(selected);
			}
		};
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
					clickNavigating = presenter.recordClicked(recordId, provider.getTaxonomyCode());
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

		tree.setDragMode(TreeDragMode.NONE);
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
					presenter.recordClicked(recordVO.getId(), null);
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

	private static class PlaceHolder extends CustomComponent {
		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}

		@Override
		public Component getCompositionRoot() {
			return super.getCompositionRoot();
		}
	}

	private class RecentTable extends BaseTable {

		private static final String MENUBAR_PROPERTY_ID = "menuBar";

		@SuppressWarnings({"rawtypes", "unchecked"})
		public RecentTable(String tableId, List<RecentItem> recentItems) {
			super(tableId);

			BeanItemContainer container = new BeanItemContainer<>(RecentItem.class, recentItems);
			setContainerDataSource(container);

			addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);

			setVisibleColumns(RecentItem.CAPTION, RecentItem.LAST_ACCESS);
			setColumnHeader(RecentItem.CAPTION, $("HomeView.recentItem.caption"));
			setColumnHeader(RecentItem.LAST_ACCESS, $("HomeView.recentItem.lastAccess"));
			setColumnExpandRatio(RecentItem.CAPTION, 1);

			addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.getButton() == MouseButton.LEFT) {
						BeanItem<RecentItem> item = (BeanItem<RecentItem>) event.getItem();
						presenter.recordClicked(item.getBean().getId(), null);
					}
				}
			});

			setCellStyleGenerator(new CellStyleGenerator() {
				@Override
				public String getStyle(Table source, Object itemId, Object propertyId) {
					String columnStyle;
					if (RecentItem.CAPTION.equals(propertyId)) {
						RecordVO recordVO = getRecordVO(itemId);
						try {
							String extension = FileIconUtils.getExtension(recordVO);
							if (extension != null) {
								columnStyle = "file-icon v-table-cell-content-file-icon-" + extension.toLowerCase();
							} else {
								columnStyle = null;
							}
						} catch (Exception e) {
							// Ignore the exception
							columnStyle = null;
						}

						String id = recordVO.getId();
						SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
						if (sessionContext.isVisited(id)) {
							String visitedStyleName = "v-table-cell-visited-link";
							columnStyle = StringUtils.isNotBlank(columnStyle) ? columnStyle + " " + visitedStyleName : visitedStyleName;
						}
					} else {
						columnStyle = null;
					}
					return columnStyle;
				}
			});

			addContextMenuAndMenuBarColumn(recentItems);
		}

		@Override
		protected CellKey getCellKey(Object itemId, Object propertyId) {
			RecentItem recentItem = (RecentItem) itemId;
			return new CellKey(recentItem.getId(), propertyId);
		}

		@Override
		public Property<?> loadContainerProperty(Object itemId, Object propertyId) {
			if (RecentItem.LAST_ACCESS.equals(propertyId)) {
				RecentItem recentItem = (RecentItem) itemId;
				String value = new JodaDateTimeToStringConverter()
						.convertToPresentation(recentItem.getLastAccess(), String.class, getSessionContext().getCurrentLocale());
				return new ObjectProperty<>(value);
			}
			return super.loadContainerProperty(itemId, propertyId);
		}

		@SuppressWarnings("unchecked")
		private RecordVO getRecordVO(Object itemId) {
			BeanItem<RecentItem> recordVOItem = (BeanItem<RecentItem>) getItem(itemId);
			RecordVO recordVO = recordVOItem.getBean().getRecord();
			return recordVO;
		}

		protected void addContextMenuAndMenuBarColumn(List<RecentItem> recentItems) {
			boolean menuBarColumnGenerated = getColumnGenerator(MENUBAR_PROPERTY_ID) != null;
			if (!menuBarColumnGenerated) {
				boolean menuBarRequired = false;
				String schemaCode = null;
				for (RecentItem recentItem : recentItems) {
					schemaCode = recentItem.getRecord().getSchema().getCode();
					List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent().getRecordMenuBarHandlers();
					for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
						if (recordMenuBarHandler.isMenuBarForSchemaCode(schemaCode)) {
							menuBarRequired = true;
							break;
						}
					}
				}
				if (menuBarRequired) {
					RecordContextMenu contextMenu = null;
					List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent()
							.getRecordContextMenuHandlers();
					for (RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
						if (recordContextMenuHandler.isContextMenuForSchemaCode(schemaCode)) {
							contextMenu = recordContextMenuHandler.getForSchemaCode(schemaCode);
							break;
						}
					}
					if (contextMenu != null) {
						contextMenu.setAsContextMenuOf(this);
						final RecordContextMenu finalContextMenu = contextMenu;
						BaseContextMenuTableListener contextMenuTableListener = new BaseContextMenuTableListener() {
							@Override
							public void onContextMenuOpenFromFooter(ContextMenuOpenedOnTableFooterEvent event) {
							}

							@Override
							public void onContextMenuOpenFromHeader(ContextMenuOpenedOnTableHeaderEvent event) {
							}

							@Override
							public void onContextMenuOpenFromRow(ContextMenuOpenedOnTableRowEvent event) {
								Object itemId = event.getItemId();
								RecordVO recordVO = getRecordVO(itemId);
								finalContextMenu.openFor(recordVO);
							}
						};
						contextMenu.addContextMenuTableListener(contextMenuTableListener);
					}

					addGeneratedColumn(MENUBAR_PROPERTY_ID, new ColumnGenerator() {
						@Override
						public Object generateCell(Table source, Object itemId, Object columnId) {
							RecordVO recordVO = getRecordVO(itemId);

							MenuBar menuBar = null;
							List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent()
									.getRecordMenuBarHandlers();
							for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
								menuBar = recordMenuBarHandler.get(recordVO);
								if (menuBar != null) {
									break;
								}
							}
							return menuBar != null ? menuBar : new Label("");
						}
					});
					setColumnHeader(MENUBAR_PROPERTY_ID, "");
				}
			}
		}
	}

}
