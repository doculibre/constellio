package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.TableProperties;
import com.constellio.model.services.configs.UserConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.google.common.collect.Lists;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.Table.ColumnReorderEvent;
import com.vaadin.ui.Table.ColumnReorderListener;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Table.HeaderClickListener;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class TableColumnsManager implements Serializable {

	protected ConstellioFactories constellioFactories;

	protected ModelLayerFactory modelLayerFactory;

	protected transient RecordServices recordServices;

	protected transient UserServices userServices;

	protected UserConfigurationsManager userConfigManager;

	protected transient User currentUser;

	protected transient MetadataSchemaTypes metadataSchemaTypes;
	
	private boolean doSort;

	public TableColumnsManager() {
		initTransientObjects();
	}

	public TableColumnsManager(Table table, String tableId) {
		initTransientObjects();
		manage(table, tableId);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();

		modelLayerFactory = constellioFactories.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		userServices = modelLayerFactory.newUserServices();
		userConfigManager = modelLayerFactory.getUserConfigurationsManager();

		String collection = null;
		if (sessionContext.getCurrentCollection() != null) {
			metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(sessionContext.getCurrentCollection());
			collection = sessionContext.getCurrentCollection();
		}

		UserVO currentUserVO = sessionContext.getCurrentUser();
		String username = null;
		if (currentUserVO != null) {
			username = currentUserVO.getUsername();
		}

		if (currentUserVO != null && username != null) {
			currentUser = userServices.getUserInCollection(username, collection);
		}
	}

	public boolean isDoSort() {
		return doSort;
	}

	public void setDoSort(boolean doSort) {
		this.doSort = doSort;
	}

	protected void decorateVisibleColumns(List<String> visibleColumnForUser, String tableId) {

	}

	public void manage(Table table, String tableId) {
		manage(table, tableId, true);
	}

	public void manage(final Table table, final String tableId, boolean doSort) {
		if (table.getColumnHeaderMode() != ColumnHeaderMode.HIDDEN) {
			table.setImmediate(true);
			table.setColumnCollapsingAllowed(true);
			table.setColumnReorderingAllowed(true);

			List<String> visibleColumnIdsForUser = getVisibleColumnIdsForCurrentUser(table, tableId);
			Collection<?> propertyIds = table.getContainerPropertyIds();
			decorateVisibleColumns(visibleColumnIdsForUser, tableId);

			TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, tableId);

			List<Object> orderedColumns = new ArrayList<>();
			for (String columnId : visibleColumnIdsForUser) {
				Object propertyId = toPropertyId(columnId, table.getVisibleColumns());
				if (propertyId != null && !orderedColumns.contains(propertyId)) {
					orderedColumns.add(propertyId);
				}
			}

			for (Object column : table.getVisibleColumns()) {
				if (!orderedColumns.contains(column)) {
					orderedColumns.add(column);
				}
			}

			if (isRightToLeft()) {
				orderedColumns = Lists.reverse(orderedColumns);
			}
			table.setVisibleColumns(orderedColumns.toArray());

			for (Object propertyId : table.getContainerPropertyIds()) {
				Align alignment = adjustAlignment(table.getColumnAlignment(propertyId));
				table.setColumnAlignment(propertyId, alignment);
			}

			for (Object propertyId : propertyIds) {
				String header = table.getColumnHeader(propertyId);
				if (StringUtils.isBlank(header)) {
					table.setColumnCollapsible(propertyId, false);
				}

				String columnId = toColumnId(propertyId);
				boolean collapsed = !visibleColumnIdsForUser.contains(columnId);
				if (table.isColumnCollapsible(propertyId) && table.isColumnCollapsed(propertyId) != collapsed) {
					table.setColumnCollapsed(propertyId, collapsed);
				}

				Integer columnWidth = properties.getColumnWidth(columnId);
				if (columnWidth != null) {
					table.setColumnWidth(propertyId, columnWidth);
				}

				if (doSort && columnId.equals(properties.getSortedColumnId())) {
					table.setSortContainerPropertyId(propertyId);
					table.setSortAscending(Boolean.TRUE.equals(properties.getSortedAscending()));
				}
			}

			table.addColumnCollapseListener(new ColumnCollapseListener() {
				@Override
				public void columnCollapseStateChange(ColumnCollapseEvent event) {
					saveVisibleColumns(table, tableId);
				}
			});

			table.addColumnReorderListener(new ColumnReorderListener() {
				@Override
				public void columnReorder(ColumnReorderEvent event) {
					saveVisibleColumns(table, tableId);
				}
			});

			table.addColumnResizeListener(new ColumnResizeListener() {
				@Override
				public void columnResize(ColumnResizeEvent event) {
					Object propertyId = event.getPropertyId();
					String columnId = toColumnId(propertyId);

					TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, tableId);
					properties.setColumnWidth(columnId, event.getCurrentWidth());
					userConfigManager.setTablePropertiesValue(currentUser, tableId, properties);
				}
			});

			table.addHeaderClickListener(new HeaderClickListener() {
				@Override
				public void headerClick(HeaderClickEvent event) {
					Object propertyId = event.getPropertyId();
					String columnId = toColumnId(propertyId);

					TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, tableId);
					if (table.getSortableContainerPropertyIds().contains(propertyId)) {
						if (columnId.equals(properties.getSortedColumnId())) {
							properties.setSortedAscending(!properties.getSortedAscending());
						} else {
							properties.setSortedColumnId(columnId);
							properties.setSortedAscending(true);
						}
						userConfigManager.setTablePropertiesValue(currentUser, tableId, properties);
					}
				}
			});
		}
	}

	private void saveVisibleColumns(final Table table, final String tableId) {
		List<String> visibleColumnIdsForUser = new ArrayList<>();

		Object[] propertyIds = table.getVisibleColumns();
		for (Object propertyId : propertyIds) {
			String columnId = toColumnId(propertyId);
			boolean collapsed = table.isColumnCollapsed(propertyId);
			if (!collapsed) {
				visibleColumnIdsForUser.add(columnId);
			}
		}

		if (isRightToLeft()) {
			visibleColumnIdsForUser = Lists.reverse(visibleColumnIdsForUser);
		}

		TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, tableId);
		properties.setVisibleColumnIds(visibleColumnIdsForUser);
		userConfigManager.setTablePropertiesValue(currentUser, tableId, properties);
	}

	protected void notifyException(Exception e) {

	}

	private List<String> getVisibleColumnIdsForCurrentUser(Table table, String tableId) {
		List<String> visibleColumnIds;
		if (currentUser != null) {
			TableProperties properties = userConfigManager.getTablePropertiesValue(currentUser, tableId);
			visibleColumnIds = properties.getVisibleColumnIds();
			if (visibleColumnIds == null) {
				visibleColumnIds = new ArrayList<>();
			}
			if (visibleColumnIds.isEmpty()) {
				visibleColumnIds = getDefaultVisibleColumnIds(table);
			}
		} else {
			visibleColumnIds = getDefaultVisibleColumnIds(table);
		}
		return visibleColumnIds;
	}

	protected List<String> getDefaultVisibleColumnIds(Table table) {
		List<String> visibleColumnIds = new ArrayList<String>();
		Object[] visibleColumns = table.getVisibleColumns();
		for (Object visibleColumn : visibleColumns) {
			visibleColumnIds.add(toColumnId(visibleColumn));
		}
		return visibleColumnIds;
	}

	protected Object toPropertyId(String columnId, Object[] propertyIds) {
		for (Object propertyId : propertyIds) {
			if (columnId.equals(propertyId.toString())) {
				return propertyId;
			}
		}
		return null;
	}

	protected String toColumnId(Object propertyId) {
		return propertyId.toString();
	}

	protected Align adjustAlignment(Align alignment) {
		Align result;
		if (isRightToLeft()) {
			if (Align.LEFT.equals(alignment)) {
				result = Align.RIGHT;
			} else if (Align.RIGHT.equals(alignment)) {
				result = Align.LEFT;
			} else if (Align.CENTER.equals(alignment)) {
				result = alignment;
			} else {
				result = Align.RIGHT;
			}
		} else {
			result = alignment;
		}
		return result;
	}

}
