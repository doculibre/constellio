package com.constellio.app.ui.framework.components.table;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordMenuBarHandler;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOContainer;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecordVOTable extends BaseTable {

	public static final String STYLE_NAME = "record-table";
	public static final String CLICKABLE_ROW_STYLE_NAME = "clickable-row";
	public static final String MENUBAR_PROPERTY_ID = "menuBar";
	private RecordContextMenu contextMenu;
	private List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
	private MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory();
	private boolean menuBarColumnAdded = false;
	private boolean menuBarColumnRemoved = false;

	public RecordVOTable() {
		super(null);
		init();
	}

	public RecordVOTable(Container dataSource) {
		this(null, dataSource);
	}

	public RecordVOTable(String caption, Container dataSource) {
		this(caption, dataSource, false);
	}

	public RecordVOTable(String caption) {
		super(null, caption);
		init();
	}

	public RecordVOTable(RecordVODataProvider dataProvider) {
		super(null);
		setContainerDataSource(new RecordVOLazyContainer(dataProvider));
		init();
	}

	public RecordVOTable(String caption, Container dataSource, Boolean collapsingAllowed) {
		super(null, caption);
		setContainerDataSource(dataSource);
		init();
		setColumnCollapsingAllowed(collapsingAllowed);
	}

	@Override
	protected String getTableId() {
		String tableId;
		if (!schemaVOs.isEmpty()) {
			StringBuilder schemaVOSuffix = new StringBuilder();
			for (MetadataSchemaVO schemaVO : schemaVOs) {
				if (schemaVOSuffix.length() > 0) {
					schemaVOSuffix.append("_");
				}
				schemaVOSuffix.append(schemaVO.getCode());
			}
			String componentId = getId();
			String navigatorState = ConstellioUI.getCurrent().getNavigator().getState();
			String navigatorStateWithoutParams;
			if (navigatorState.indexOf("/") != -1) {
				navigatorStateWithoutParams = StringUtils.substringBefore(navigatorState, "/");
			} else {
				navigatorStateWithoutParams = navigatorState;
			}
			tableId = navigatorStateWithoutParams + "." + schemaVOSuffix;
			if (componentId != null) {
				tableId += "." + componentId;
			}
		} else {
			tableId = null;
		}
		return tableId;
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		return new RecordVOTableColumnsManager();
	}

	private void init() {
		addStyleName(STYLE_NAME);

		setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String columnStyle;
				if (isTitleColumn(propertyId)) {
					RecordVO recordVO = getRecordVOForTitleColumn(getItem(itemId));
					columnStyle = getTitleColumnStyle(recordVO);

					String id = recordVO == null ? null: recordVO.getId();
					SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
					if (id != null && sessionContext.isVisited(id)) {
						String visitedStyleName = "v-table-cell-visited-link";
						columnStyle = StringUtils.isNotBlank(columnStyle) ? columnStyle + " " + visitedStyleName : visitedStyleName;
					}
				} else {
					columnStyle = null;
				}
				return columnStyle;
			}

			private boolean isTitleColumn(Object id) {
				if (CommonMetadataBuilder.TITLE.equals(id)) {
					return true;
				}
				if (id instanceof MetadataVO) {
					MetadataVO metadata = (MetadataVO) id;
					return metadata.codeMatches(CommonMetadataBuilder.TITLE);
				}
				return false;
			}
		});
	}

	protected String getTitleColumnStyle(RecordVO recordVO) {
		String style;
		if (recordVO != null) {
			try {
				String extension = FileIconUtils.getExtension(recordVO);
				if (extension != null && !isDecomList(recordVO)) {
					style = "file-icon v-table-cell-content-file-icon-" + extension.toLowerCase();
				} else {
					style = null;
				}
			} catch (Exception e) {
				// Do Nothing;
				style = null;
			}
		} else {
			style = null;
		}
		return style;
	}

	private boolean isDecomList(RecordVO recordVO) {
		if (recordVO.getSchema() != null) {
			return recordVO.getSchema().getCode().startsWith(DecommissioningList.SCHEMA_TYPE);
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	protected RecordVO getRecordVOForTitleColumn(Item item) {
		if (item instanceof RecordVOItem) {
			return ((RecordVOItem) item).getRecord();
		}
		if (item instanceof BeanItem) {
			Object bean = ((BeanItem) item).getBean();
			return (RecordVO) bean;
		}
		return null;
	}

	public boolean isContextMenuPossible() {
		return true;
	}

	public final List<MetadataSchemaVO> getSchemas() {
		return schemaVOs;
	}

	public final void setMetadataDisplayFactory(MetadataDisplayFactory metadataDisplayFactory) {
		this.metadataDisplayFactory = metadataDisplayFactory;
	}

	@Override
	public void setContainerDataSource(Container newDataSource) {
		super.setContainerDataSource(newDataSource);
		findSchemaVOs(newDataSource);
		initSchemaVOs();
	}

	@Override
	protected CellKey getCellKey(Object itemId, Object propertyId) {
		RecordVO recordVO;
		Item item = getItem(itemId);
		if (item instanceof RecordVOItem) {
			RecordVOItem recordVOItem = (RecordVOItem) item;
			recordVO = recordVOItem.getRecord();
		} else {
			recordVO = null;
		}

		CellKey cellKey;
		if (recordVO != null) {
			String recordId = recordVO.getId();
			if (propertyId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) propertyId;
				cellKey = new CellKey(recordId, metadataVO.getCode());
			} else {
				cellKey = new CellKey(recordId, propertyId);
			}
		} else {
			cellKey = null;
		}
		return cellKey;
	}

	@Override
	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		Property<?> containerProperty;
		if (propertyId instanceof MetadataVO) {
			RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
			RecordVO recordVO = recordVOItem.getRecord();
			MetadataVO metadataVO = (MetadataVO) propertyId;
			MetadataValueVO metadataValue = recordVO.getMetadataValue(metadataVO);
			Component metadataDisplay;
			if (metadataValue != null) {
				metadataDisplay = buildMetadataComponent(itemId, metadataValue, recordVO);
			} else {
				metadataDisplay = new Label("");
			}
			boolean instance = metadataDisplay instanceof Label;
			boolean matchCode = metadataVO.codeMatches(Schemas.TITLE_CODE);
			if (instance && matchCode) {
				RecordVO titleRecordVO = getRecordVOForTitleColumn(getItem(itemId));
				if (titleRecordVO != null) {
					MetadataSchemaVO recordSchemaVO = titleRecordVO.getSchema();
					String prefix = SchemaCaptionUtils.getCaptionForSchema(recordSchemaVO.getCode(), getLocale());
					Label titleLabel = (Label) metadataDisplay;
					String titleForRecordVO = getTitleForRecordVO(titleRecordVO, prefix, titleLabel.getValue());
					titleLabel.setValue(titleForRecordVO);
				}
			}
			containerProperty = new ObjectProperty<>(metadataDisplay, Component.class);
		} else {
			containerProperty = super.loadContainerProperty(itemId, propertyId);
		}
		return containerProperty;
	}
	
	public Property<?> getMetadataProperty(Object itemId, String metadataCode) {
		Property<?> match = null;
		for (Object propertyId : getContainerPropertyIds()) {
			if (propertyId instanceof MetadataVO && ((MetadataVO) propertyId).codeMatches(metadataCode)) {
				match = getContainerProperty(itemId, propertyId);
				break;
			}
		}
		return match;
	}

	protected String getTitleForRecordVO(RecordVO titleRecordVO, String prefix, String title) {
		return StringUtils.isNotBlank(prefix) ? prefix + " " + title : title;
	}

	protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue, RecordVO recordVO) {
		return metadataDisplayFactory.build(recordVO, metadataValue);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> type;
		if (propertyId instanceof MetadataVO) {
			//			MetadataVO metadataVO = (MetadataVO) propertyId;
			//			Class<?> javaType = metadataVO.getJavaType();
			//			if (LocalDateTime.class.isAssignableFrom(javaType) || LocalDate.class.isAssignableFrom(javaType) || Number.class.isAssignableFrom(javaType) || Boolean.class.isAssignableFrom(javaType)) {
			//				type = javaType;
			//			} else {
			//				type = Component.class;
			//			}
			type = Component.class;
		} else {
			type = super.getType(propertyId);
		}
		return type;
	}

	private void findSchemaVOs(Container container) {
		if (container instanceof RecordVOLazyContainer) {
			RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) container;
			schemaVOs = recordVOLazyContainer.getSchemas();
		} else if (container instanceof ContainerAdapter) {
			ContainerAdapter<?> containerAdapter = (ContainerAdapter<?>) container;
			findSchemaVOs(containerAdapter.getNestedContainer());
		} else if (container instanceof ViewableRecordVOContainer) {
			ViewableRecordVOContainer viewableRecordVOContainer = (ViewableRecordVOContainer) container;
			findSchemaVOs(viewableRecordVOContainer.getRecordVOContainer());
		}
	}

	private void initSchemaVOs() {
		if (schemaVOs != null && !schemaVOs.isEmpty()) {
			MetadataVO titleMetadata = schemaVOs.get(0).getMetadata(Schemas.TITLE.getCode());
			setColumnExpandRatio(titleMetadata, 1);
			if (isContextMenuPossible()) {
				addContextMenu();
				addMenuBarColumn();
			}
		}
	}
	
	public void setExpandTitleColumn(boolean expand) {
		if (schemaVOs != null && !schemaVOs.isEmpty()) {
			MetadataVO titleMetadata = schemaVOs.get(0).getMetadata(Schemas.TITLE.getCode());
			if (expand) {
				setColumnExpandRatio(titleMetadata, 1);
			} else {
				setColumnExpandRatio(titleMetadata, 0);
			}
		}	
	} 
	
	public void setTitleColumnWidth(int width) {
		if (schemaVOs != null && !schemaVOs.isEmpty()) {
			MetadataVO titleMetadata = schemaVOs.get(0).getMetadata(Schemas.TITLE.getCode());
			setColumnWidth(titleMetadata, width);
		}	
	} 

	protected RecordVO getRecordVO(Object itemId) {
		RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
		return recordVOItem.getRecord();
	}

	protected void addContextMenu() {
		for (MetadataSchemaVO schemaVO : schemaVOs) {
			String schemaCode = schemaVO.getCode();
			List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
			for (RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
				if (recordContextMenuHandler.isContextMenuForSchemaCode(schemaCode)) {
					contextMenu = recordContextMenuHandler.getForSchemaCode(schemaCode);
					break;
				}
			}
			if (contextMenu != null) {
				contextMenu.setAsContextMenuOf(this);
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
						contextMenu.openFor(recordVO);
					}
				};
				contextMenu.addContextMenuTableListener(contextMenuTableListener);
				break;
			}
		}
	}

	public boolean isMenuBarColumnRemoved() {
		return menuBarColumnRemoved;
	}

	public void removeMenuBarColumn() {
		menuBarColumnRemoved = true;
		boolean menuBarColumnGenerated = getColumnGenerator(MENUBAR_PROPERTY_ID) != null;
		if (menuBarColumnGenerated) {
			removeGeneratedColumn(MENUBAR_PROPERTY_ID);
		}
	}

	protected void addMenuBarColumn() {
		boolean menuBarColumnGenerated = getColumnGenerator(MENUBAR_PROPERTY_ID) != null;
		if (!menuBarColumnGenerated && !menuBarColumnRemoved) {
			boolean menuBarRequired = false;
			for (MetadataSchemaVO schemaVO : schemaVOs) {
				String schemaCode = schemaVO.getCode();
				List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent().getRecordMenuBarHandlers();
				for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
					if (recordMenuBarHandler.isMenuBarForSchemaCode(schemaCode)) {
						menuBarRequired = true;
						break;
					}
				}
			}
			if (menuBarRequired) {
				addGeneratedColumn(MENUBAR_PROPERTY_ID, new ColumnGenerator() {
					@Override
					public Object generateCell(Table source, Object itemId, Object columnId) {
						Component cellContent;
						Item item = getItem(itemId);
						RecordVO recordVO = getRecordVOForTitleColumn(item);
						String recordId = recordVO != null ? recordVO.getId() : "_NULL_";
						CellKey cellKey = new CellKey(recordId, columnId);
						Property<?> cellProperty = cellProperties.get(cellKey);
						if (cellProperty != null) {
							cellContent = (Component) cellProperty.getValue();
						} else {
							if (recordVO != null) {
								MenuBar menuBar = null;
								List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent().getRecordMenuBarHandlers();
								for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
									menuBar = recordMenuBarHandler.get(recordVO);
									if (menuBar != null) {
										break;
									}
								}
								if (menuBar == null) {
									cellContent = new Label("");
								} else {
									cellContent = menuBar;
								}
							} else {
								cellContent = new Label("");
							}
							cellProperties.put(cellKey, new ObjectProperty<Object>(cellContent));
						}
						return cellContent;
					}
				});
				setColumnHeader(MENUBAR_PROPERTY_ID, "");
				setColumnCollapsible(MENUBAR_PROPERTY_ID, false);
				menuBarColumnAdded = true;
			}
		}
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		List<Object> containerPropertyIds = new ArrayList<>(super.getContainerPropertyIds());
		if (menuBarColumnAdded) {
			containerPropertyIds.add(MENUBAR_PROPERTY_ID);
		}
		return containerPropertyIds;
	}

	@Override
	public void addItemClickListener(final ItemClickListener listener) {
		addStyleName(CLICKABLE_ROW_STYLE_NAME);

		super.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT || getContextMenu() != null) {
					listener.itemClick(event);
				}
			}
		});
	}

	public void setColumnCollapsed(String metadataCode, boolean collapsed) {
		if (!isColumnCollapsingAllowed()) {
			return;
		}
		Object[] visibleColumnIds = getVisibleColumns();
		for (Object visibleColumnId : visibleColumnIds) {
			if (visibleColumnId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) visibleColumnId;
				if (metadataVO.getCode().contains(metadataCode)) {
					setColumnCollapsed(visibleColumnId, collapsed);
				}
			}
		}
	}

	public ContextMenu getContextMenu() {
		return contextMenu;
	}

}
