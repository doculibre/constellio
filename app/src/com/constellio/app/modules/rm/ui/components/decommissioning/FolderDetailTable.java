package com.constellio.app.modules.rm.ui.components.decommissioning;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.model.entities.structures.TableProperties;
import com.vaadin.data.Container;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.CHECKBOX;
import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.FOLDER;
import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.FOLDER_ID;
import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.ORDER;
import static com.constellio.app.modules.rm.ui.components.decommissioning.FolderDetailTableGenerator.VALIDATION_CHECKBOX;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class FolderDetailTable extends BaseTable {

	public FolderDetailTable(String tableId, String caption, Container container) {
		super(tableId, caption, container);
		setPageLength(container.size());
		setWidth("100%");
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
					defaultVisibleColumnIds.addAll(asList(CHECKBOX, ORDER, VALIDATION_CHECKBOX, FOLDER_ID, FOLDER));
				}
				table.setColumnCollapsible(CHECKBOX, false);
				return defaultVisibleColumnIds;
			}

			@Override
			public void manage(Table table, String tableId) {
				super.manage(table, tableId);
				List<String> visibleIds = getDefaultVisibleColumnIds(table);
				Collection<?> propertyIds = Arrays.asList(table.getVisibleColumns());
				for (Object propertyId : propertyIds) {
					String columnId = toColumnId(propertyId);
					boolean visible = visibleIds.contains(columnId);
					table.setColumnCollapsed(propertyId, !visible);
				}
			}

		};
	}
}
