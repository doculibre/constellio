package com.constellio.app.ui.framework.components.table;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordMenuBarHandler;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
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

import java.util.ArrayList;
import java.util.List;

public class RecordVOTable extends BaseTable {

	public static final String STYLE_NAME = "record-table";
	public static final String CLICKABLE_ROW_STYLE_NAME = "clickable-row";
	private List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
	private MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory();

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
		RecordVO recordVO;
		if (item instanceof RecordVOItem) {
			recordVO = ((RecordVOItem) item).getRecord();
		} else if (item instanceof BeanItem) {
			Object bean = ((BeanItem) item).getBean();
			recordVO = (RecordVO) bean;
		} else {
			recordVO = null;
		}
		return recordVO;
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
		if (container instanceof RecordVOContainer) {
			RecordVOContainer recordVOContainer = (RecordVOContainer) container;
			schemaVOs = recordVOContainer.getSchemas();
		} else if (container instanceof ContainerAdapter) {
			ContainerAdapter<?> containerAdapter = (ContainerAdapter<?>) container;
			// Recursive call
			findSchemaVOs(containerAdapter.getNestedContainer());
		}
	}

	private void initSchemaVOs() {
		if (schemaVOs != null && !schemaVOs.isEmpty()) {
			MetadataVO titleMetadata = schemaVOs.get(0).getMetadata(Schemas.TITLE.getCode());
			setColumnExpandRatio(titleMetadata, 1);
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

	@SuppressWarnings("rawtypes")
	protected RecordVO getRecordVO(Object itemId) {
		RecordVO recordVO;
		Item item = getItem(itemId);
		if (item instanceof RecordVOItem) {
			recordVO = ((RecordVOItem) item).getRecord();
		} else if (item instanceof BeanItem) {
			Object bean = ((BeanItem) item).getBean();
			recordVO = (RecordVO) bean;
		} else {
			recordVO = null;
		}
		return recordVO;
	}

	@Override
	protected void contextMenuOpened(ContextMenu contextMenu, Object itemId) {
		RecordVO recordVO = getRecordVO(itemId);
		RecordContextMenu recordContextMenu = (RecordContextMenu) contextMenu;
		recordContextMenu.openFor(recordVO);
	}

	@Override
	public boolean isMenuBarColumn() {
		boolean menuBarForSchema = false;
		for (MetadataSchemaVO schemaVO : schemaVOs) {
			String schemaCode = schemaVO.getCode();
			List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent().getRecordMenuBarHandlers();
			for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
				if (recordMenuBarHandler.isMenuBarForSchemaCode(schemaCode)) {
					menuBarForSchema = true;
					break;
				}
			}
		}
		return menuBarForSchema;
	}

	@Override
	protected MenuBar newMenuBar(Object itemId) {
		MenuBar menuBar = null;
		List<RecordMenuBarHandler> recordMenuBarHandlers = ConstellioUI.getCurrent().getRecordMenuBarHandlers();
		for (RecordMenuBarHandler recordMenuBarHandler : recordMenuBarHandlers) {
			Item item = getItem(itemId);
			RecordVO recordVO = getRecordVOForTitleColumn(item);
			menuBar = recordMenuBarHandler.get(recordVO);
			if (menuBar != null) {
				break;
			}
		}
		return menuBar;
	}

	@Override
	public boolean isContextMenuPossible() {
		boolean contextMenuForSchema = false;
		loop1:
		for (MetadataSchemaVO schemaVO : schemaVOs) {
			String schemaCode = schemaVO.getCode();
			List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
			for (RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
				contextMenuForSchema = recordContextMenuHandler.isContextMenuForSchemaCode(schemaCode);
				if (contextMenuForSchema) {
					break loop1;
				}
			}
		}
		return contextMenuForSchema;
	}

	@Override
	protected ContextMenu newContextMenu() {
		RecordContextMenu contextMenu = null;
		for (MetadataSchemaVO schemaVO : schemaVOs) {
			String schemaCode = schemaVO.getCode();
			List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
			for (RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
				if (recordContextMenuHandler.isContextMenuForSchemaCode(schemaCode)) {
					contextMenu = recordContextMenuHandler.getForSchemaCode(schemaCode);
					break;
				}
			}
		}
		return contextMenu;
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

	@Override
	protected SelectionManager newSelectionManager() {
		return new RecordVOSelectionManager();
	}

	public class RecordVOSelectionManager implements SelectionManager {

		@SuppressWarnings({"rawtypes", "unchecked"})
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

		@Override
		public boolean isAllItemsSelected() {
			List<Object> listValue = ensureListValue();
			return listValue.containsAll(getAllRecordVOs());
		}

		@Override
		public boolean isAllItemsDeselected() {
			List<Object> listValue = ensureListValue();
			return listValue.isEmpty();
		}

		@Override
		public boolean isSelected(Object itemId) {
			List<Object> listValue = ensureListValue();
			RecordVO recordVO = getRecordVO(itemId);
			return listValue.contains(recordVO);
		}

		private List<RecordVO> getAllRecordVOs() {
			List<RecordVO> listValue = new ArrayList<>();
			for (Object itemId : getItemIds()) {
				RecordVO recordVO = getRecordVO(itemId);
				listValue.add(recordVO);
			}
			return listValue;
		}

		@Override
		public void selectionChanged(SelectionChangeEvent event) {
			if (event.isAllItemsSelected()) {
				List<RecordVO> listValue = getAllRecordVOs();
				setValue(listValue);
			} else if (event.isAllItemsDeselected()) {
				setValue(new ArrayList<>());
			} else {
				Object selectedItemId = event.getSelectedItemId();
				Object deselectedItemId = event.getDeselectedItemId();
				List<Object> listValue = ensureListValue();
				if (selectedItemId != null) {
					RecordVO recordVO = getRecordVO(selectedItemId);
					listValue.add(recordVO);
				} else if (deselectedItemId != null) {
					RecordVO recordVO = getRecordVO(deselectedItemId);
					listValue.remove(recordVO);
				}
				setValue(listValue);
			}
		}
		
	}

}
