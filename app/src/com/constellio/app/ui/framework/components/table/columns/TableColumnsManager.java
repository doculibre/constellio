package com.constellio.app.ui.framework.components.table.columns;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnCollapseEvent;
import com.vaadin.ui.Table.ColumnCollapseListener;
import com.vaadin.ui.Table.ColumnReorderEvent;
import com.vaadin.ui.Table.ColumnReorderListener;

public class TableColumnsManager implements Serializable {
	
	protected ConstellioFactories constellioFactories;
	
	protected ModelLayerFactory modelLayerFactory;
	
	protected transient RecordServices recordServices;
	
	protected transient UserServices userServices;
	
	protected transient User currentUser;
	
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
		
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String username = currentUserVO.getUsername();
		
		currentUser = userServices.getUserInCollection(username, collection);
	}
	
	public void manage(final Table table, final String tableId) {
		table.setColumnCollapsingAllowed(true);
		table.setColumnReorderingAllowed(true);

		List<String> visibleColumnIdsForUser = getVisibleColumnIdsForCurrentUser(table, tableId);
		Collection<?> propertyIds = table.getContainerPropertyIds();
		for (Object propertyId : propertyIds) {
			String columnId = toColumnId(propertyId);
			boolean collapsed = !visibleColumnIdsForUser.contains(columnId); 
			table.setColumnCollapsed(propertyId, collapsed);
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
	
	protected void notifyException(Exception e) {
		
	}
	
	private List<String> getVisibleColumnIdsForCurrentUser(Table table, String tableId) {
		List<String> visibleColumnIds = currentUser.getVisibleTableColumnsFor(tableId);
		if (visibleColumnIds == null) {
			visibleColumnIds = new ArrayList<>();
		}
		if (visibleColumnIds.isEmpty()) {
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

}
