package com.constellio.app.modules.rm.ui.components.decommissioning;

import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.CHECKBOX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.model.entities.structures.TableProperties;
import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class FolderDetailTable extends BaseTable {
	
	private List<String> defaultGeneratedColumnIds = new ArrayList<>();

	public FolderDetailTable(String tableId, String caption, Container container) {
		super(tableId, caption, container);
		setPageLength(container.size());
		setWidth("100%");
	}

	@Override
	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
		addGeneratedColumn(id, generatedColumn, true);
	}
	
	public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn, boolean defaultGeneratedColumn) {
		super.addGeneratedColumn(id, generatedColumn);
		if (defaultGeneratedColumn && id instanceof String && !defaultGeneratedColumnIds.contains((String) id)) {
			defaultGeneratedColumnIds.add((String) id);
		}
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		return new TableColumnsManager() {
			@Override
			protected List<String> getDefaultVisibleColumnIds(Table table) {
				List<String> defaultVisibleColumnIds = new ArrayList<>();
				TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, getTableId());
				List<String> userVisibleColumns = properties.getVisibleColumnIds();
				if (userVisibleColumns != null) {
					defaultVisibleColumnIds.addAll(userVisibleColumns);
				} else {
					defaultVisibleColumnIds.addAll(defaultGeneratedColumnIds);
				}
				table.setColumnCollapsible(CHECKBOX, false);
				return defaultVisibleColumnIds;
			}

			@Override
			public void manage(Table table, String tableId, boolean doSort) {
				super.manage(table, tableId, doSort);
				List<String> visibleIds = getDefaultVisibleColumnIds(table);
				Collection<?> propertyIds = Arrays.asList(table.getVisibleColumns());
				for (Object propertyId : propertyIds) {
					String columnId = toColumnId(propertyId);
					if (table.isColumnCollapsible(propertyId)) {
						boolean visible = visibleIds.contains(columnId);
						table.setColumnCollapsed(propertyId, !visible);
					}
				}
			}

		};
	}
}
