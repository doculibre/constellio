package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentWindow;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskViewImpl;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NCssLayout;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.BaseTable.PagingControls;
import com.constellio.app.ui.framework.components.table.BaseTable.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.table.BaseTable.SelectionChangeListener;
import com.constellio.app.ui.framework.components.table.BaseTable.SelectionManager;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable.RecordVOSelectionManager;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.schemaRecords.DisplaySchemaRecordWindow;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.ui.i18n.i18n.$;

//@com.vaadin.annotations.JavaScript({ "theme://jquery/jquery-2.1.4.min.js" })
public class ViewableRecordVOTablePanel extends I18NHorizontalLayout implements BrowserWindowResizeListener {

	public static final int MAX_SELECTION_SIZE = 10000;

	public static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");

	public static enum TableMode {
		LIST, TABLE;
	}

	private VerticalLayout tableLayout;

	private I18NCssLayout tableButtonsLayout;

	private I18NHorizontalLayout selectionButtonsLayout;

	private I18NHorizontalLayout actionAndModeButtonsLayout;

	private I18NHorizontalLayout actionButtonsLayout;

	private VerticalLayout closeButtonViewerMetadataLayout;

	private RecordVOContainer recordVOContainer;

	private BaseTable table;

	private ViewerMetadataPanel viewerMetadataPanel;

	private BaseButton previousButton;

	private BaseButton nextButton;

	private ListModeButton listModeButton;

	private TableModeButton tableModeButton;

	private SelectDeselectAllButton selectDeselectAllToggleButton;

	private Label countLabel;

	private Label selectedItemCountLabel;

	private BaseButton closeViewerButton;

	private Object selectedItemId;

	private RecordVO selectedRecordVO;

	private Object previousItemId;

	private Object nextItemId;

	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

	private List<ItemClickListener> itemClickListeners = new ArrayList<>();

	private List<TableCompressListener> tableCompressListeners = new ArrayList<>();

	private List<TableModeChangeListener> tableModeChangeListeners = new ArrayList<>();

	private TableMode tableMode = TableMode.LIST;

	private PagingControls pagingControls;

	private List<Object> tableModeVisibleColumns = new ArrayList<>();

	private Map<Object, String> tableModeColumnHeaders = new HashMap<>();

	private Map<Object, Integer> tableModeColumnExpandRatios = new HashMap<>();

	private Button quickActionButton;

	private RecordListMenuBar selectionActionsMenuBar;

	private RecordListMenuBar initialSelectionActionsMenuBar = null;

	public ViewableRecordVOTablePanel(RecordVOContainer container) {
		this(container, TableMode.LIST, null);
	}

	public ViewableRecordVOTablePanel(RecordVOContainer container, TableMode tableMode) {
		this(container, tableMode, null);
	}

	public ViewableRecordVOTablePanel(RecordVOContainer container, TableMode tableMode,
									  RecordListMenuBar recordListMenuBar) {
		this.recordVOContainer = container;
		this.tableMode = tableMode != null ? tableMode : TableMode.LIST;
		this.initialSelectionActionsMenuBar = recordListMenuBar;
		buildUI();
	}

	@Override
	public void attach() {
		super.attach();
		Page.getCurrent().addBrowserWindowResizeListener(this);
	}

	@Override
	public void detach() {
		Page.getCurrent().removeBrowserWindowResizeListener(this);
		super.detach();
	}

	public RecordVOContainer getRecordVOContainer() {
		return recordVOContainer;
	}

	private void buildUI() {
		setWidth("100%");
		setSpacing(true);
		addStyleName("viewable-record-table-panel");
		setId(UUID.randomUUID().toString());

		boolean empty = recordVOContainer.size() == 0;
		table = buildResultsTable();
		if (isSelectColumn()) {
			selectDeselectAllToggleButton = newSelectDeselectAllToggleButton();
			selectDeselectAllToggleButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectDeselectAllToggleButton.setVisible(!empty);
		}

		countLabel = new Label();
		countLabel.addStyleName("count-label");
		countLabel.setVisible(false);

		selectedItemCountLabel = new Label();
		selectedItemCountLabel.addStyleName("count-label");
		selectedItemCountLabel.setVisible(false);

		viewerMetadataPanel = buildViewerMetadataPanel();
		listModeButton = buildListModeButton();
		tableModeButton = buildTableModeButton();
		previousButton = buildPreviousButton();
		nextButton = buildNextButton();
		closeViewerButton = buildCloseViewerButton();

		tableLayout = new VerticalLayout();
		tableLayout.addStyleName("viewable-record-panel-table-layout");
		//		tableLayout.setHeight("100%");

		tableButtonsLayout = new I18NCssLayout();
		tableButtonsLayout.addStyleName("table-buttons-layout");
		tableButtonsLayout.setWidth("100%");
		//		tableButtonsLayout.setSpacing(true);

		selectionButtonsLayout = new I18NHorizontalLayout();
		selectionButtonsLayout.addStyleName("selection-buttons-layout");
		selectionButtonsLayout.setSpacing(true);

		actionButtonsLayout = new I18NHorizontalLayout();
		actionButtonsLayout.setSpacing(true);

		actionAndModeButtonsLayout = new I18NHorizontalLayout();
		actionAndModeButtonsLayout.addStyleName("action-mode-buttons-layout");
		actionAndModeButtonsLayout.addComponents(actionButtonsLayout);

		if (isSelectColumn()) {
			selectionButtonsLayout.addComponent(selectDeselectAllToggleButton);
		}
		selectionButtonsLayout.addComponents(selectedItemCountLabel, countLabel);
		selectionButtonsLayout.addComponents(previousButton, nextButton);

		actionAndModeButtonsLayout.addComponents(listModeButton, tableModeButton);

		tableButtonsLayout.addComponents(selectionButtonsLayout, actionAndModeButtonsLayout);

		//		tableButtonsLayout.setComponentAlignment(selectionButtonsLayout, Alignment.TOP_LEFT);
		//		tableButtonsLayout.setComponentAlignment(actionAndModeButtonsLayout, Alignment.TOP_RIGHT);

		tableLayout.addComponent(tableButtonsLayout);
		tableLayout.addComponent(table);
		if (table.isPaged()) {
			tableLayout.addComponent(pagingControls = table.createPagingControls());
		}

		closeButtonViewerMetadataLayout = new VerticalLayout(closeViewerButton, viewerMetadataPanel);
		closeButtonViewerMetadataLayout.addStyleName("close-button-viewer-metadata-layout");
		closeButtonViewerMetadataLayout.setId("close-button-viewer-metadata-layout");
		//		closeButtonViewerMetadataLayout.setHeight("100%");
		closeButtonViewerMetadataLayout.setComponentAlignment(closeViewerButton, Alignment.TOP_RIGHT);
		//		closeButtonViewerMetadataLayout.setWidthUndefined();

		addComponents(tableLayout, closeButtonViewerMetadataLayout);
		addTableCompressListener(new TableCompressListener() {
			@Override
			public void tableCompressChange(TableCompressEvent event) {
				if (isCompressionSupported()) {
					((ViewableRecordVOTable) table).setCompressed(event.isCompressed());
				}
			}
		});
		closeButtonViewerMetadataLayout.setVisible(isCompressionSupported());
		adjustTableExpansion();
	}

	public void setCountCaption(String caption) {
		countLabel.setValue(caption);
		countLabel.setVisible(StringUtils.isNotBlank(caption));
	}

	public void setSelectedCountCaption(int numberOfSelected) {
		String key = numberOfSelected <= 1 ? "ViewableRecordVOTablePanel.nbSelectedElement1" : "ViewableRecordVOTablePanel.nbSelectedElements";
		String totalCount = $(key, numberOfSelected);

		selectedItemCountLabel.setValue(totalCount);
		selectedItemCountLabel.setVisible(numberOfSelected > 0);
	}

	public void setQuickActionButton(List<Button> button) {
		for (Button baseButton : button) {
			setQuickActionButton(baseButton);
		}
	}

	public void setQuickActionButton(Button button) {
		this.quickActionButton = button;
		quickActionButton.addStyleName(ValoTheme.BUTTON_LINK);
		quickActionButton.addStyleName("quick-action-button");
		if (quickActionButton instanceof BaseButton) {
			((BaseButton) quickActionButton).setCaptionVisibleOnMobile(false);
		}
		actionButtonsLayout.addComponent(quickActionButton, 0);
		actionButtonsLayout.setComponentAlignment(quickActionButton, Alignment.TOP_LEFT);
	}

	public void setSelectionActionButtons() {
		if (isSelectColumn()) {
			if (initialSelectionActionsMenuBar == null) {
				selectionActionsMenuBar = new RecordListMenuBar(getMenuItemProvider(), $("ViewableRecordVOTablePanel.selectionActions"), excludedMenuItemInDefaultSelectionActionButtons(), getMainView());
			} else {
				selectionActionsMenuBar = initialSelectionActionsMenuBar;
				selectionActionsMenuBar.setRecordProvider(getMenuItemProvider());
			}

			addSelectionActionsMenuBarToView();
		}
	}

	protected List<String> excludedMenuItemInDefaultSelectionActionButtons() {
		return Collections.emptyList();
	}

	protected MenuItemRecordProvider getMenuItemProvider() {
		return new MenuItemRecordProvider() {
			@Override
			public List<Record> getRecords() {
				return ViewableRecordVOTablePanel.this.getSelectedRecords();
			}

			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}
		};
	}

	private void addSelectionActionsMenuBarToView() {
		if (!isSelectionActionMenuBar()) {
			return;
		}

		selectionActionsMenuBar.addStyleName("selection-action-menu-bar");
		selectionActionsMenuBar.setAutoOpen(false);
		actionButtonsLayout.addComponent(selectionActionsMenuBar, 0);
		actionButtonsLayout.setComponentAlignment(selectionActionsMenuBar, Alignment.TOP_RIGHT);

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				selectionActionsMenuBar.buildMenuItems();
			}
		});
	}

	public boolean isSelectionActionMenuBar() {
		return true;
	}

	public List<Record> getSelectedRecords() {
		List<Record> selectedRecords;
		if (table.getSelectionManager() instanceof RecordVOSelectionManager) {
			RecordVOSelectionManager recordVOSelectionManager = (RecordVOSelectionManager) table.getSelectionManager();
			selectedRecords = recordVOSelectionManager.getSelectedRecords();
		} else {
			selectedRecords = new ArrayList<>();
			List<Object> selectedItemIds = table.getSelectionManager().getAllSelectedItemIds();
			List<RecordVO> recordVOS = recordVOContainer.getRecordsVO(selectedItemIds);
			for (RecordVO recordVO : recordVOS) {
				selectedRecords.add(recordVO.getRecord());
			}
		}
		return selectedRecords;
	}

	int computeCompressedWidth() {
		return Page.getCurrent().getBrowserWindowWidth() > 1400 ? 650 : 500;
	}

	boolean isCompressionSupported() {
		return ResponsiveUtils.isDesktop() && !isNested() && tableMode == TableMode.LIST;
	}

	protected boolean isNested() {
		return false;
	}

	protected Component newSearchResultComponent(Object itemId) {
		return null;
	}

	//	private void ensureHeight(Object itemId) {
	//		int l = table.getPageLength();
	//		int index = table.indexOfId(itemId);
	//		int indexToSelectAbove = index - (l / 2);
	//		if (indexToSelectAbove < 0) {
	//			indexToSelectAbove = 0;
	//		}
	//		table.setCurrentPageFirstItemIndex(indexToSelectAbove);
	//	}

	void adjustTableExpansion() {
		if (tableMode == TableMode.LIST) {
			if (isCompressionSupported()) {
				String compressedStyleName = "viewable-record-table-compressed";
				if (selectedItemId != null) {
					if (!closeButtonViewerMetadataLayout.isVisible()) {
						int compressedWidth = computeCompressedWidth();
						if (table != null) {
							int searchResultPropertyWidth = compressedWidth - BaseTable.SELECT_PROPERTY_WIDTH - ViewableRecordVOContainer.THUMBNAIL_WIDTH - 3;
							table.setColumnWidth(ViewableRecordVOContainer.SEARCH_RESULT_PROPERTY, searchResultPropertyWidth);
							table.addStyleName(compressedStyleName);
						}
						closeButtonViewerMetadataLayout.setVisible(true);
						tableLayout.setWidth(compressedWidth + "px");
						setExpandRatio(tableLayout, 0);
						setExpandRatio(closeButtonViewerMetadataLayout, 1);
					}
				} else {
					if (closeButtonViewerMetadataLayout.isVisible()) {
						if (table != null) {
							table.setColumnWidth(ViewableRecordVOContainer.SEARCH_RESULT_PROPERTY, -1);
							table.removeStyleName(compressedStyleName);
						}
						closeButtonViewerMetadataLayout.setVisible(false);
						tableLayout.setWidth("100%");
						setExpandRatio(tableLayout, 1);
						setExpandRatio(closeButtonViewerMetadataLayout, 0);
					}
				}
			}
		} else {
			closeButtonViewerMetadataLayout.setVisible(false);
			tableLayout.setWidth("100%");
			setExpandRatio(tableLayout, 1);
			setExpandRatio(closeButtonViewerMetadataLayout, 0);
		}
	}

	private void adjustHeight() {
		if (closeButtonViewerMetadataLayout.isVisible()) {
			ConstellioUI.getCurrent().runAsync(new Runnable() {
				@Override
				public void run() {
					ConstellioUI.getCurrent().access(new Runnable() {
						@Override
						public void run() {
							final String functionId = "zeFunction";
							JavaScript.getCurrent().addFunction(functionId,
									new JavaScriptFunction() {
										@Override
										public void call(JsonArray arguments) {
											int tableBodyWrapperHeight = Integer.parseInt(StringUtils.removeEnd(arguments.getString(0), "px"));
											int metadataPanelHeight = Integer.parseInt(StringUtils.removeEnd(arguments.getString(1), "px"));
											int adjustedHeight = Math.max(tableBodyWrapperHeight, metadataPanelHeight) + 400;
											ViewableRecordVOTablePanel.this.setHeight(adjustedHeight + "px");
										}
									});

							StringBuilder js = new StringBuilder();
							//							js.append("setTimeout(function() { ");
							//							js.append("try { ");
							js.append("  var tableBodyWrapperHeight =  document.getElementById('" + getId() + "').getElementsByClassName('v-table-body-wrapper')[0].style.height;");
							js.append("  var metadataPanelHeight = document.getElementById('" + viewerMetadataPanel.getId() + "').getElementsByClassName('v-tabsheet-tabsheetpanel')[0].style.height;");
							js.append(functionId + "(tableBodyWrapperHeight, metadataPanelHeight);");
							//							js.append("console.info(tableBodyWrapperHeight + ', ' + metadataPanelHeight);");
							//							js.append("} catch (err) { log.error(err.message); } ");
							//							js.append(" }, 100);");
							JavaScript.getCurrent().execute(js.toString());
						}
					});
				}
			}, 10, this);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private BaseTable buildResultsTable() {
		BaseTable resultsTable;
		if (tableMode == TableMode.LIST) {
			ViewableRecordVOContainer viewableRecordVOContainer = new ViewableRecordVOContainer(recordVOContainer) {
				@Override
				protected Component getRecordDisplay(Object itemId) {
					Component recordDisplay = ViewableRecordVOTablePanel.this.newSearchResultComponent(itemId);
					if (recordDisplay == null) {
						recordDisplay = super.getRecordDisplay(itemId);
					}
					return recordDisplay;
				}
			};

			final ViewableRecordVOTable viewableRecordVOTable = new ViewableRecordVOTable(viewableRecordVOContainer) {
				@Override
				public boolean isSelectColumn() {
					return ViewableRecordVOTablePanel.this.isSelectColumn();
				}

				@Override
				public boolean isIndexColumn() {
					return ViewableRecordVOTablePanel.this.isIndexColumn();
				}

				@Override
				protected SelectionManager newSelectionManager() {
					SelectionManager selectionManager = ViewableRecordVOTablePanel.this.newSelectionManager();

					if (selectionManager == null) {
						selectionManager = super.newSelectionManager();
					}

					SelectionManager finalSelectionManager = createSelectionManagerWithSelectedCountCaption(selectionManager);

					return finalSelectionManager;
				}

				private SelectionManager createSelectionManagerWithSelectedCountCaption(
						SelectionManager selectionManager) {
					final SelectionManager finalSelectionManager = selectionManager;
					SelectionManager selectionManagerWithSelectedCount = new SelectionManager() {
						@Override
						public List<Object> getAllSelectedItemIds() {
							return finalSelectionManager.getAllSelectedItemIds();
						}

						@Override
						public boolean isAllItemsSelected() {
							return finalSelectionManager.isAllItemsSelected();
						}

						@Override
						public boolean isAllItemsDeselected() {
							return finalSelectionManager.isAllItemsDeselected();
						}

						@Override
						public boolean isSelected(Object itemId) {
							return finalSelectionManager.isSelected(itemId);
						}

						@Override
						public void selectionChanged(SelectionChangeEvent event) {
							finalSelectionManager.selectionChanged(event);
							setSelectedCountCaption(getSelectedRecords().size());
						}
					};
					return selectionManagerWithSelectedCount;
				}

				@Override
				public boolean isPaged() {
					return ViewableRecordVOTablePanel.this.isPagedInListMode();
				}

				@Override
				protected RecordVO getRecordVOForTitleColumn(Item item) {
					RecordVO recordVO = ViewableRecordVOTablePanel.this.getRecordVOForTitleColumn(item);
					if (recordVO == null) {
						recordVO = super.getRecordVOForTitleColumn(item);
					}
					return recordVO;
				}
			};
			viewableRecordVOTable.setWidth("100%");

			resultsTable = viewableRecordVOTable;
			resultsTable.setContainerDataSource(new ContainerAdapter(viewableRecordVOContainer) {
				@Override
				public Property getContainerProperty(Object itemId, Object propertyId) {
					Property result = super.getContainerProperty(itemId, propertyId);
					Object propertyValue = result.getValue();
					if (propertyValue instanceof Component) {
						List<ReferenceDisplay> referenceDisplays = ComponentTreeUtils.getChildren((Component) propertyValue, ReferenceDisplay.class);
						for (ReferenceDisplay referenceDisplay : referenceDisplays) {
							for (Object listenerObject : new ArrayList<>(referenceDisplay.getListeners(ClickEvent.class))) {
								referenceDisplay.removeClickListener((ClickListener) listenerObject);
							}
						}
						List<ConstellioAgentLink> constellioAgentLinks = ComponentTreeUtils.getChildren((Component) propertyValue, ConstellioAgentLink.class);
						for (ConstellioAgentLink constellioAgentLink : constellioAgentLinks) {
							for (Object listenerObject : new ArrayList<>(constellioAgentLink.getAgentLink().getListeners(ClickEvent.class))) {
								constellioAgentLink.getAgentLink().removeClickListener((ClickListener) listenerObject);
							}
						}
					}
					return new ObjectProperty<>(propertyValue);
				}
			});

			resultsTable.setSelectable(true);
			resultsTable.setMultiSelect(false);
		} else {
			resultsTable = new RecordVOTable(recordVOContainer) {
				@Override
				protected String getTableId() {
					String tableId = super.getTableId();
					if (tableId == null) {
						tableId = getClass().getName() + ".tableMode";
					}
					return tableId;
				}

				@Override
				protected RecordVO getRecordVOForTitleColumn(Item item) {
					RecordVO recordVO = ViewableRecordVOTablePanel.this.getRecordVOForTitleColumn(item);
					if (recordVO == null) {
						recordVO = super.getRecordVOForTitleColumn(item);
					}
					return recordVO;
				}

				@Override
				public boolean isSelectColumn() {
					return ViewableRecordVOTablePanel.this.isSelectColumn();
				}

				@Override
				protected SelectionManager newSelectionManager() {
					SelectionManager selectionManager = ViewableRecordVOTablePanel.this.newSelectionManager();
					if (selectionManager == null) {
						selectionManager = super.newSelectionManager();
					}
					return selectionManager;
				}

				@Override
				public boolean isIndexColumn() {
					return ViewableRecordVOTablePanel.this.isIndexColumn();
				}

				@Override
				public boolean isPaged() {
					// Never paged in table mode
					return false;
				}

				@Override
				public boolean isMenuBarColumn() {
					return ViewableRecordVOTablePanel.this.isMenuBarColumn();
				}
			};
			resultsTable.setWidth("100%");
			resultsTable.addStyleName("viewable-record-table-table-mode");

			if (!tableModeVisibleColumns.isEmpty()) {
				resultsTable.setVisibleColumns(tableModeVisibleColumns.toArray(new Object[0]));
			}
			for (Object propertyId : tableModeColumnHeaders.keySet()) {
				resultsTable.setColumnHeader(propertyId, tableModeColumnHeaders.get(propertyId));
			}
			for (Object propertyId : tableModeColumnExpandRatios.keySet()) {
				resultsTable.setColumnExpandRatio(propertyId, tableModeColumnExpandRatios.get(propertyId));
			}
		}

		final CellStyleGenerator cellStyleGenerator = resultsTable.getCellStyleGenerator();
		resultsTable.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String baseStyle = cellStyleGenerator != null ? cellStyleGenerator.getStyle(source, itemId, propertyId) : "";
				if (StringUtils.isNotBlank(baseStyle)) {
					baseStyle += " ";
				}
				String tableModeName = tableMode == TableMode.LIST ? "listMode" : "tableMode";
				baseStyle += "viewer-results-table-" + tableModeName + " ";
				baseStyle += "viewer-results-table-" + propertyId + " ";
				baseStyle += "viewer-results-table-" + tableModeName + "-" + propertyId + " ";
				return baseStyle + "viewer-results-table-row-" + itemId;
			}
		});

		resultsTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				rowClicked(event);
			}
		});
		for (ItemClickListener listener : itemClickListeners) {
			resultsTable.addItemClickListener(listener);
		}
		for (SelectionChangeListener listener : selectionChangeListeners) {
			resultsTable.addSelectionChangeListener(listener);
		}
		resultsTable.removeStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		resultsTable.setAlwaysRecalculateColumnWidths(true);

		return resultsTable;
	}

	public boolean isMenuBarColumn() {
		return false;
	}

	public TableMode getTableMode() {
		return tableMode;
	}

	public void setTableMode(TableMode tableMode) {
		if (tableMode == null) {
			tableMode = TableMode.LIST;
		}
		if (tableMode != this.tableMode) {
			this.tableMode = tableMode;
			if (table != null) {
				if (tableMode == TableMode.TABLE) {
					closeViewer();
				}

				BaseTable tableBefore = table;
				table = buildResultsTable();
				if (tableBefore != null) {
					table.setValue(tableBefore.getValue());
					for (SelectionChangeListener selectionChangeListener : tableBefore.getSelectionChangeListeners()) {
						table.addSelectionChangeListener(selectionChangeListener);
					}
				}

				tableLayout.replaceComponent(tableBefore, table);
				if (pagingControls != null) {
					tableLayout.removeComponent(pagingControls);
				}
				if (table.isPaged()) {
					tableLayout.addComponent(pagingControls = table.createPagingControls());
				}
				adjustTableExpansion();

				updateTableButtonsAfterTableModeChange(tableBefore);
			}

			TableModeChangeEvent event = new TableModeChangeEvent(tableMode, null);
			for (TableModeChangeListener listener : tableModeChangeListeners) {
				listener.tableModeChanged(event);
			}
		}
	}

	private SelectDeselectAllButton newSelectDeselectAllToggleButton() {
		return table.newSelectDeselectAllToggleButton("", "");
	}

	private void updateTableButtonsAfterTableModeChange(BaseTable tableBefore) {
		if (isSelectColumn()) {
			SelectDeselectAllButton selectDeselectAllToggleButtonBefore = selectDeselectAllToggleButton;
			selectDeselectAllToggleButton = newSelectDeselectAllToggleButton();
			selectDeselectAllToggleButton.addStyleName(ValoTheme.BUTTON_LINK);
			if (selectDeselectAllToggleButtonBefore != null && selectDeselectAllToggleButtonBefore.isSelectAllMode() != selectDeselectAllToggleButton.isSelectAllMode()) {
				selectDeselectAllToggleButton.setSelectAllMode(selectDeselectAllToggleButtonBefore.isSelectAllMode());
			}
			selectionButtonsLayout.replaceComponent(selectDeselectAllToggleButtonBefore, selectDeselectAllToggleButton);
		}
	}

	void rowClicked(ItemClickEvent event) {
		Object itemId = event.getItemId();
		if (isCompressionSupported()) {
			selectRecordVO(itemId, event, false);
			previousButton.setVisible(itemId != null);
			nextButton.setVisible(itemId != null);
		} else {
			RecordVO recordVO = getRecordVO(itemId);
			displayInWindowOrNavigate(recordVO);
		}
	}

	boolean isSelected(Object itemId) {
		return selectedItemId != null && selectedItemId.equals(itemId);
	}

	Object getSelectedItemId() {
		return selectedItemId;
	}

	public RecordVO getRecordVO(Object itemId) {
		return recordVOContainer.getRecordVO(itemId);
	}

	public List<MetadataSchemaVO> getSchemas() {
		return recordVOContainer.getSchemas();
	}

	private void displayRecordVOInWindow(RecordVO recordVO) {
		ViewWindow viewWindow;
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			viewWindow = new DisplayDocumentWindow(recordVO);
		} else {
			viewWindow = new DisplaySchemaRecordWindow(recordVO);
		}
		viewWindow.addCloseListener(new Window.CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				if (selectedRecordVO != null && getTableMode() == TableMode.LIST) {
					refreshMetadata();
				} else {
					recordVOContainer.forceRefresh();
				}
			}
		});
		ConstellioUI.getCurrent().addWindow(viewWindow);
	}

	private void navigateToRecordVO(RecordVO recordVO) {
		new ReferenceDisplay(recordVO).click();
	}

	protected boolean isDisplayInWindowOnSelection(RecordVO recordVO) {
		boolean displayInWindowOnSelection;
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayInWindowOnSelection = false;
		} else {
			displayInWindowOnSelection = !ResponsiveUtils.isDesktop();
		}
		return displayInWindowOnSelection;
	}

	private void displayInWindowOrNavigate(RecordVO recordVO) {
		if (isDisplayInWindowOnSelection(recordVO)) {
			displayRecordVOInWindow(recordVO);
		} else {
			navigateToRecordVO(recordVO);
		}
	}

	void selectRecordVO(Object itemId, ItemClickEvent event, boolean reload) {
		Object newSelectedItemId = itemId;
		table.setValue(newSelectedItemId);

		Boolean compressionChange;
		if (selectedItemId == null && newSelectedItemId != null) {
			compressionChange = true;
		} else {
			compressionChange = null;
		}

		if (!newSelectedItemId.equals(this.selectedItemId) || reload) {
			selectedItemId = newSelectedItemId;
			if (reload) {
				recordVOContainer.forceRefresh();
			}
			selectedRecordVO = getRecordVO(selectedItemId);
			previousItemId = recordVOContainer.prevItemId(itemId);
			nextItemId = recordVOContainer.nextItemId(itemId);

			if (!isCompressionSupported() && selectedRecordVO != null) {
				displayInWindowOrNavigate(selectedRecordVO);
			} else {
				previousButton.setEnabled(previousItemId != null);
				nextButton.setEnabled(nextItemId != null);

				viewerMetadataPanel.setRecordVO(selectedRecordVO);
				if (compressionChange != null) {
					TableCompressEvent tableCompressEvent = new TableCompressEvent(event, compressionChange);
					for (TableCompressListener tableCompressListener : tableCompressListeners) {
						tableCompressListener.tableCompressChange(tableCompressEvent);
					}
				}
				adjustTableExpansion();
			}
		}
	}

	private void closeViewer() {
		selectedItemId = null;

		TableCompressEvent tableCompressEvent = new TableCompressEvent(null, false);
		for (TableCompressListener tableCompressListener : tableCompressListeners) {
			tableCompressListener.tableCompressChange(tableCompressEvent);
		}

		adjustTableExpansion();
		previousButton.setVisible(false);
		nextButton.setVisible(false);
	}

	protected boolean isSelectColumn() {
		return false;
	}

	protected boolean isIndexColumn() {
		return false;
	}

	protected SelectionManager newSelectionManager() {
		return null;
	}

	protected boolean isPagedInListMode() {
		return false;
	}

	private ViewerMetadataPanel buildViewerMetadataPanel() {
		return new ViewerMetadataPanel();
	}

	private ListModeButton buildListModeButton() {
		return new ListModeButton();
	}

	private TableModeButton buildTableModeButton() {
		return new TableModeButton();
	}

	private BaseButton buildPreviousButton() {
		String caption = $("ViewableRecordVOTablePanel.previous");
		BaseButton previousButton = new IconButton(FontAwesome.CHEVRON_LEFT, caption) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (previousItemId != null) {
					selectRecordVO(previousItemId, null, false);
				}
			}
		};
		previousButton.addStyleName("chevron");
		previousButton.addStyleName("previous-button");
		previousButton.setWidth("16px");
		previousButton.addExtension(new NiceTitle(caption, false));
		previousButton.setVisible(false);
		return previousButton;
	}

	private BaseButton buildNextButton() {
		String caption = $("ViewableRecordVOTablePanel.next");
		BaseButton nextButton = new IconButton(FontAwesome.CHEVRON_RIGHT, caption) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (nextItemId != null) {
					selectRecordVO(nextItemId, null, false);
				}
			}
		};
		nextButton.addStyleName("chevron");
		nextButton.addStyleName("next-button");
		nextButton.setWidth("16px");
		nextButton.addExtension(new NiceTitle(caption, false));
		nextButton.setVisible(false);
		return nextButton;
	}

	private BaseButton buildCloseViewerButton() {
		BaseButton closeViewerButton = new IconButton(FontAwesome.TIMES, $("ViewableRecordVOTablePanel.closeViewer")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				closeViewer();
			}
		};
		closeViewerButton.setId("close-viewer-button");
		closeViewerButton.addStyleName(closeViewerButton.getId());
		return closeViewerButton;
	}

	private void refreshMetadata() {
		RecordServices recordServices = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory().newRecordServices();
		Record selectedRecord = recordServices.getDocumentById(selectedRecordVO.getId());
		selectedRecordVO = new RecordToVOBuilder().build(selectedRecord, VIEW_MODE.DISPLAY, ConstellioUI.getCurrentSessionContext());
		selectRecordVO(selectedItemId, null, true);
	}

	public List<TableCompressListener> getTableCompressListeners() {
		return tableCompressListeners;
	}

	public void addTableCompressListener(TableCompressListener listener) {
		if (isCompressionSupported() && !tableCompressListeners.contains(listener)) {
			tableCompressListeners.add(listener);
		}
	}

	public void removeTableCompressListener(TableCompressListener listener) {
		tableCompressListeners.remove(listener);
	}

	public List<TableModeChangeListener> getTableModeChangeListeners() {
		return tableModeChangeListeners;
	}

	public void addTableModeChangeListener(TableModeChangeListener listener) {
		if (!tableModeChangeListeners.contains(listener)) {
			tableModeChangeListeners.add(listener);
		}
	}

	public void removeTableModeChangeListener(TableModeChangeListener listener) {
		tableModeChangeListeners.remove(listener);
	}

	public List<ItemClickListener> getItemClickListeners() {
		return itemClickListeners;
	}

	public void addItemClickListener(ItemClickListener listener) {
		if (!itemClickListeners.contains(listener)) {
			itemClickListeners.add(listener);
			if (table != null) {
				table.addItemClickListener(listener);
			}
		}
	}

	public void removeItemClickListener(ItemClickListener listener) {
		itemClickListeners.remove(listener);
		if (table != null) {
			table.removeItemClickListener(listener);
		}
	}

	public List<SelectionChangeListener> getSelectionChangeListeners() {
		return selectionChangeListeners;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		if (!selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.add(listener);
			if (table != null) {
				table.addSelectionChangeListener(listener);
			}
		}
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		selectionChangeListeners.remove(listener);
		if (table != null) {
			table.removeSelectionChangeListener(listener);
		}
	}

	public HorizontalLayout createPagingControls() {
		return table.createPagingControls();
	}

	public void setItemsPerPageValue(int value) {
		if (table.isPaged()) {
			pagingControls.setItemsPerPageValue(value);
		}
	}

	public void select(Object itemId) {
		table.select(itemId);
	}

	public void deselect(Object itemId) {
		table.deselect(itemId);
	}

	public void selectAll() {
		table.selectAll();
	}

	public void deselectAll() {
		table.deselectAll();
	}

	public void selectCurrentPage() {
		table.selectCurrentPage();
	}

	public void deselectCurrentPage() {
		table.deselectCurrentPage();
	}

	protected boolean contextMenuOpened(ContextMenu contextMenu, Object itemId) {
		return false;
	}

	public void setVisibleColumns(Object... visibleColumns) {
		if (visibleColumns != null) {
			tableModeVisibleColumns = Arrays.asList(visibleColumns);
		} else {
			tableModeVisibleColumns = Collections.emptyList();
		}
		if (table != null && tableMode == TableMode.TABLE) {
			table.setVisibleColumns(visibleColumns);
		}
	}

	public void setColumnHeader(Object propertyId, String header) {
		tableModeColumnHeaders.put(propertyId, header);
		if (table != null && tableMode == TableMode.TABLE) {
			table.setColumnHeader(propertyId, header);
		}
	}

	public void setColumnExpandRatio(Object propertyId, int expandRatio) {
		tableModeColumnExpandRatios.put(propertyId, expandRatio);
		if (table != null && tableMode == TableMode.TABLE) {
			table.setColumnExpandRatio(propertyId, expandRatio);
		}
	}

	protected RecordVO getRecordVOForTitleColumn(Item item) {
		return null;
	}

	public BaseTable getActualTable() {
		return table;
	}

	private class ViewerMetadataPanel extends VerticalLayout {

		private VerticalLayout mainLayout;

		public ViewerMetadataPanel() {
			buildUI();
		}

		private void setRecordVO(RecordVO recordVO) {
			mainLayout.removeAllComponents();
			this.removeStyleName("nested-view");

			if (recordVO != null) {
				Component panelContent;
				String schemaTypeCode = recordVO.getSchema().getTypeCode();
				if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(recordVO, true, false);
					view.enter(null);
					view.addEditWindowCloseListener(new Window.CloseListener() {
						@Override
						public void windowClose(CloseEvent e) {
							refreshMetadata();
						}
					});
					panelContent = view;
				} else if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayFolderViewImpl view = new DisplayFolderViewImpl(recordVO, true, false);
					view.enter(null);
					panelContent = view;
				} else if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayTaskViewImpl view = new DisplayTaskViewImpl(recordVO, true, false);
					view.enter(null);
					panelContent = view;
				} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
					DisplayContainerViewImpl view = new DisplayContainerViewImpl(recordVO, false, true);
					view.enter(null);
					panelContent = view;
				} else {
					UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
					panelContent = new RecordDisplayFactory(currentUser).build(recordVO, true);
					this.addStyleName("nested-view");
				}
				mainLayout.addComponent(panelContent);
			}
		}

		private void buildUI() {
			setId(UUID.randomUUID().toString());
			setWidth("100%");
			addStyleName(ValoTheme.PANEL_BORDERLESS);
			addStyleName("viewer-metadata-panel");

			mainLayout = new VerticalLayout();
			mainLayout.addStyleName("viewer-metadata-panel-main-layout");
			mainLayout.setSizeFull();
			addComponent(mainLayout);
		}

		@Override
		public void beforeClientResponse(boolean initial) {
			super.beforeClientResponse(initial);
			//			adjustHeight();
		}

	}

	public Button getCloseViewerButton() {
		return closeViewerButton;
	}

	public class TableCompressEvent implements Serializable {

		private final ItemClickEvent itemClickEvent;

		private final boolean compressed;

		public TableCompressEvent(ItemClickEvent itemClickEvent, boolean compressed) {
			this.itemClickEvent = itemClickEvent;
			this.compressed = compressed;
		}

		public ItemClickEvent getItemClickEvent() {
			return itemClickEvent;
		}

		public boolean isCompressed() {
			return compressed;
		}

	}

	public interface TableCompressListener extends Serializable {

		void tableCompressChange(TableCompressEvent event);

	}

	public class TableModeChangeEvent implements Serializable {

		private Component component;

		private TableMode tableMode;

		public TableModeChangeEvent(TableMode tableMode, Component component) {
			this.tableMode = tableMode;
			this.component = component;
		}

		public TableMode getTableMode() {
			return tableMode;
		}

		public Component getComponent() {
			return component;
		}
	}

	public interface TableModeChangeListener extends Serializable {

		void tableModeChanged(TableModeChangeEvent event);

	}

	private class ListModeButton extends IconButton {

		public ListModeButton() {
			super(FontAwesome.ALIGN_JUSTIFY, $("ViewableRecordVOTablePanel.tableMode.list"));
			addStyleName(ValoTheme.BUTTON_LINK);
			addStyleName("list-mode-button");
			updateState();
			addTableModeChangeListener(new TableModeChangeListener() {
				@Override
				public void tableModeChanged(TableModeChangeEvent event) {
					updateState();
				}
			});
		}

		private void updateState() {
			setEnabled(tableMode == TableMode.TABLE);
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			setTableMode(TableMode.LIST);
		}

	}

	private class TableModeButton extends IconButton {

		public TableModeButton() {
			super(FontAwesome.TH, $("ViewableRecordVOTablePanel.tableMode.table"));
			addStyleName(ValoTheme.BUTTON_LINK);
			addStyleName("table-mode-button");
			updateState();
			addTableModeChangeListener(new TableModeChangeListener() {
				@Override
				public void tableModeChanged(TableModeChangeEvent event) {
					updateState();
				}
			});
		}

		private void updateState() {
			setEnabled(tableMode == TableMode.LIST);
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			setTableMode(TableMode.TABLE);
		}

	}

	public boolean scrollIntoView(Integer itemIndex, String recordId) {
		boolean scrolledIntoView;
		if (itemIndex < recordVOContainer.size()) {
			List<?> itemIds = recordVOContainer.getItemIds(itemIndex, 1);
			if (!itemIds.isEmpty()) {
				Object itemId = itemIds.get(0);
				RecordVO recordVO = recordVOContainer.getRecordVO(itemIndex);
				if (recordVO != null && recordVO.getId().equals(recordId)) {
					table.setCurrentPageFirstItemIndex(itemIndex);
					if (isCompressionSupported()) {
						selectRecordVO(itemId, null, false);
					}
					scrolledIntoView = true;
				} else {
					scrolledIntoView = false;
				}
			} else {
				scrolledIntoView = false;
			}
		} else {
			scrolledIntoView = false;
		}
		return scrolledIntoView;
	}

	@Override
	public void browserWindowResized(BrowserWindowResizeEvent event) {
		// TODO Auto-generated method stub
	}


	public BaseView getMainView() {
		return null;
	}
}
