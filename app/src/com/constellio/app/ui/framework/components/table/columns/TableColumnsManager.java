package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.Table.ColumnReorderEvent;
import com.vaadin.ui.Table.ColumnReorderListener;
import org.apache.commons.lang3.ArrayUtils;

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

	protected transient User currentUser;

	protected transient MetadataSchemaTypes metadataSchemaTypes;

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

	protected void decorateVisibleColumns(List<String> visibleColumnForUser, String tableId) {

	}

	public void manage(final Table table, final String tableId) {
		if (table.getColumnHeaderMode() != ColumnHeaderMode.HIDDEN) {
			table.setColumnCollapsingAllowed(true);
			table.setColumnReorderingAllowed(true);

			Object[] visibleColumns = table.getVisibleColumns();
			if (isRightToLeft()) {
				ArrayUtils.reverse(visibleColumns);
				table.setVisibleColumns(visibleColumns);


				for (Object propertyId : table.getContainerPropertyIds()) {
					Align alignment = adjustAlignment(table.getColumnAlignment(propertyId));
					table.setColumnAlignment(propertyId, alignment);
				}
			}

			List<String> visibleColumnIdsForUser = getVisibleColumnIdsForCurrentUser(table, tableId);
			Collection<?> propertyIds = table.getContainerPropertyIds();
			decorateVisibleColumns(visibleColumnIdsForUser, tableId);

			for (Object propertyId : propertyIds) {
				String columnId = toColumnId(propertyId);

				boolean collapsed = !visibleColumnIdsForUser.contains(columnId);
				if (!collapsed || table.isColumnCollapsible(columnId)) {
					table.setColumnCollapsed(propertyId, collapsed);
				}
			}

			table.addColumnCollapseListener(new ColumnCollapseListener() {
				@Override
				public void columnCollapseStateChange(ColumnCollapseEvent event) {
					Object propertyId = event.getPropertyId();
					String columnId = toColumnId(propertyId);
					boolean collapsed = table.isColumnCollapsed(propertyId);
					List<String> visibleColumnIdsForUser = getVisibleColumnIdsForCurrentUser(table, tableId);
					if (collapsed) {
						visibleColumnIdsForUser.remove(columnId);
					} else if (!visibleColumnIdsForUser.contains(columnId)) {
						visibleColumnIdsForUser.add(columnId);
					}
					currentUser.setVisibleTableColumns(tableId, visibleColumnIdsForUser);
					try {
						recordServices.update(currentUser);
					} catch (RecordServicesException e) {
						notifyException(e);
					}
				}
			});

			table.addColumnReorderListener(new ColumnReorderListener() {
				@Override
				public void columnReorder(ColumnReorderEvent event) {
					if (currentUser == null) {
						return;
					}
					Object[] visibleColumnIds = table.getVisibleColumns();
					List<String> visibleColumnIdsForUser = new ArrayList<>();
					for (Object visiblePropertyId : visibleColumnIds) {
						String columnId = toColumnId(visiblePropertyId);
						if (!table.isColumnCollapsed(visiblePropertyId)) {
							visibleColumnIdsForUser.add(columnId);
						}
					}
					currentUser.setVisibleTableColumns(tableId, visibleColumnIdsForUser);
					try {
						recordServices.update(currentUser);
					} catch (RecordServicesException e) {
						notifyException(e);
					}
				}
			});
		}
	}

	protected void notifyException(Exception e) {

	}

	private List<String> getVisibleColumnIdsForCurrentUser(Table table, String tableId) {
		List<String> visibleColumnIds;
		if (currentUser != null) {
			visibleColumnIds = currentUser.getVisibleTableColumnsFor(tableId);
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

	protected String toColumnId(Object propertyId) {
		return propertyId.toString();
	}

	private Align adjustAlignment(Align alignment) {
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
