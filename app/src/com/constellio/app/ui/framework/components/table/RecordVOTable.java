/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components.table;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

public class RecordVOTable extends Table {
	
	public static final String STYLE_NAME = "record-table";
	
	public static final String CLICKABLE_ROW_STYLE_NAME = "clickable-row";
	
	private RecordContextMenu contextMenu;
	
	private MetadataSchemaVO schemaVO;
	
	private MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory();

	public RecordVOTable() {
		super();
		init();
	}

	public RecordVOTable(String caption, Container dataSource, Boolean collapsingAllowed) {
		super(caption, dataSource);
		init();
		setColumnCollapsingAllowed(collapsingAllowed);
	}

	public RecordVOTable(String caption, Container dataSource) {
		this(caption, dataSource, false);
	}

	public RecordVOTable(String caption) {
		super(caption);
		init();
	}
	
	public RecordVOTable(RecordVODataProvider dataProvider) {
		setContainerDataSource(new RecordVOLazyContainer(dataProvider));
		init();
	}
	
	private void init() {
		addStyleName(STYLE_NAME);	
		setColumnCollapsingAllowed(true);
		
		setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				String style;
				if (propertyId instanceof MetadataVO) {
					MetadataVO metadataVO = (MetadataVO) propertyId;
					if (metadataVO.codeMatches(Schemas.TITLE_CODE)) {
						RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
						RecordVO recordVO = recordVOItem.getRecord();
						String extension = FileIconUtils.getExtension(recordVO);
						if (extension != null) {
							style = "file-icon-" + extension;
						} else {
							style = null;
						}
					} else {
						style = null;
					}
				} else {
					style = null;
				}
				return style;
			}
		});
	}
	
	public final MetadataSchemaVO getSchema() {
		return schemaVO;
	}

	public final void setSchema(MetadataSchemaVO schemaVO) {
		this.schemaVO = schemaVO;
		initSchemaVO();
	}

	public final void setMetadataDisplayFactory(MetadataDisplayFactory metadataDisplayFactory) {
		this.metadataDisplayFactory = metadataDisplayFactory;
	}

	@Override
	public void setContainerDataSource(Container newDataSource) {
		super.setContainerDataSource(newDataSource);
		findSchemaVO(newDataSource);
		initSchemaVO();
	}
	
	@Override
	public Property<?> getContainerProperty(final Object itemId, final Object propertyId) {
		Property<?> containerProperty;
		if (propertyId instanceof MetadataVO) {
			MetadataVO metadataVO = (MetadataVO) propertyId;
			RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
			RecordVO recordVO = recordVOItem.getRecord();
			MetadataValueVO metadataValue = recordVO.getMetadataValue(metadataVO);
			Component metadataDisplay = buildMetadataComponent(metadataValue, recordVO);
			if ((metadataDisplay instanceof Label) && metadataVO.codeMatches(Schemas.TITLE.getCode())) {
				String prefix = SchemaCaptionUtils.getCaptionForSchema(schemaVO.getCode());
				if (StringUtils.isNotBlank(prefix)) {
					Label titleLabel = (Label) metadataDisplay;
					titleLabel.setValue(prefix + " " + titleLabel.getValue());
				}
			}
			containerProperty = new ObjectProperty<Component>(metadataDisplay, Component.class);
		} else {
			containerProperty = super.getContainerProperty(itemId, propertyId);
		}
		return containerProperty;
	}

	protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
		return metadataDisplayFactory.build(recordVO, metadataValue);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> type;
		if (propertyId instanceof MetadataVO) {
			type = Component.class;
		} else {
			type = super.getType(propertyId);
		}
		return type;
	}

	private void findSchemaVO(Container container) {
		if (container instanceof RecordVOLazyContainer) {
			RecordVOLazyContainer recordVOLazyContainer = (RecordVOLazyContainer) container;
			schemaVO = recordVOLazyContainer.getSchema();
		} else if (container instanceof ContainerAdapter) {
			ContainerAdapter<?> containerAdapter = (ContainerAdapter<?>) container;
			findSchemaVO(containerAdapter.getNestedContainer());
		}
	}

	private void initSchemaVO() {
		if (schemaVO != null) {
			MetadataVO titleMetadata = schemaVO.getMetadata(Schemas.TITLE.getCode());
			setColumnExpandRatio(titleMetadata, 1);			
			addContextMenu();
		}
	}
	
	protected RecordVO getRecordVO(Object itemId) {
		RecordVOItem recordVOItem = (RecordVOItem) getItem(itemId);
		return recordVOItem.getRecord();
	}
	
	protected void addContextMenu() {
		String schemaCode = getSchema().getCode();
		List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
		for (final RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
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
		}
	}

	@Override
	public void addItemClickListener(final ItemClickListener listener) {
		addStyleName(CLICKABLE_ROW_STYLE_NAME);
		
		super.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT || contextMenu != null) {
					listener.itemClick(event);
				}
			}
		});
	}

	public void collapseColumn(String metadataCode) {
		if (!isColumnCollapsingAllowed()){
			return;
		}
		Object[] metadataList = getVisibleColumns();
		for(Object metadata : metadataList){
			MetadataVO metadataVO = (MetadataVO) metadata;
			if (metadataVO.getCode().contains(metadataCode)){
				setColumnCollapsed(metadata, true);
			}
		}
	}
}
