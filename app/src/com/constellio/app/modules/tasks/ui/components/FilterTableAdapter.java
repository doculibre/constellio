package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.FilterTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class FilterTableAdapter extends FilterTable {
	private final Table adaptedTable;

	public FilterTableAdapter(final Table adaptedTable, FilterDecorator decorator, FilterGenerator generator) {
		this.adaptedTable = adaptedTable;

		if (decorator != null) {
			setFilterDecorator(decorator);
		}
		if (generator != null) {
			setFilterGenerator(generator);
		}

		Collection<?> listeners = adaptedTable.getListeners(AttachEvent.class);
		for (Object al : CollectionUtils.emptyIfNull(listeners)) {
			((AttachListener) al).attach(new AttachEvent(this));
		}

		setContainerDataSource(adaptedTable.getContainerDataSource());

		Object[] visibleColumns = adaptedTable.getVisibleColumns();
		for (Object propertyId : ArrayUtils.nullToEmpty(visibleColumns)) {
			addGeneratedColumn(propertyId.toString(), new ColumnGeneratorAdapter(adaptedTable, adaptedTable.getColumnGenerator(propertyId.toString())));
		}

		String[] columnsHeaders = adaptedTable.getColumnHeaders();

		/*Rearange columns to set the menu bar at the end*/
		ArrayList<String> columnHeadersAsList = new ArrayList<>(Arrays.asList(columnsHeaders));
		ArrayList<Object> visibleColumnsAsList = new ArrayList<>(Arrays.asList(visibleColumns));
		if (visibleColumnsAsList.contains(BaseTable.MENUBAR_PROPERTY_ID)) {
			int indexMenuBar = visibleColumnsAsList.indexOf(BaseTable.MENUBAR_PROPERTY_ID);
			String menubarHeader = columnHeadersAsList.get(indexMenuBar);
			Object menubar = visibleColumnsAsList.get(indexMenuBar);

			columnHeadersAsList.remove(indexMenuBar);
			visibleColumnsAsList.remove(indexMenuBar);

			if (isRightToLeft()) {
				columnHeadersAsList.add(0, menubarHeader);
				visibleColumnsAsList.add(0, menubar);
			} else {
				columnHeadersAsList.add(menubarHeader);
				visibleColumnsAsList.add(menubar);
			}
		}

		columnsHeaders = columnHeadersAsList.toArray(new String[0]);
		visibleColumns = visibleColumnsAsList.toArray();

		setVisibleColumns(visibleColumns);
		setColumnHeaders(columnsHeaders);

		setColumnReorderingAllowed(adaptedTable.isColumnReorderingAllowed());
		setColumnCollapsingAllowed(adaptedTable.isColumnCollapsingAllowed());

		Collection<?> propertyIds = adaptedTable.getContainerPropertyIds();
		for (Object propertyId : CollectionUtils.emptyIfNull(propertyIds)) {
			setColumnCollapsed(propertyId, adaptedTable.isColumnCollapsed(propertyId));
		}

		listeners = adaptedTable.getListeners(Table.ColumnCollapseEvent.class);
		for (Object tccl : CollectionUtils.emptyIfNull(listeners)) {
			addColumnCollapseListener(new ColumnCollapseListenerAdapter((Table.ColumnCollapseListener) tccl));
		}

		listeners = adaptedTable.getListeners(Table.ColumnReorderEvent.class);
		for (Object tccl : CollectionUtils.emptyIfNull(listeners)) {
			addColumnReorderListener(new ColumnReorderListenerAdapter((Table.ColumnReorderListener) tccl));
		}

		listeners = adaptedTable.getListeners(ItemClickEvent.class);
		for (Object tccl : CollectionUtils.emptyIfNull(listeners)) {
			addItemClickListener((ItemClickEvent.ItemClickListener) tccl);
		}

		setCellStyleGenerator(new CellStyleGeneratorAdapter(adaptedTable, adaptedTable.getCellStyleGenerator()));

		addColumnCollapseListener(new ColumnCollapseListener() {
			@Override
			public void columnCollapseStateChange(ColumnCollapseEvent event) {
				Object propertyId = event.getPropertyId();
				boolean collapsed = FilterTableAdapter.this.isColumnCollapsed(propertyId);
				adaptedTable.setColumnCollapsed(propertyId, collapsed);
			}
		});

		addStyleName(adaptedTable.getStyleName());

		resetFilters();

		setSizeFull();

		setFilterBarVisible(true);
	}

	@Override
	public void attach() {
		super.attach();
	}

	@Override
	public ColumnGenerator getColumnGenerator(Object columnId) throws IllegalArgumentException {
		Table.ColumnGenerator columnGenerator = adaptedTable.getColumnGenerator(columnId);
		if (columnGenerator instanceof CustomTable.ColumnGenerator) {
			return (ColumnGenerator) columnGenerator;
		}

		return super.getColumnGenerator(columnId);
	}

	@Override
	public Container getContainerDataSource() {
		return adaptedTable.getContainerDataSource();
	}

	@Override
	public String[] getColumnHeaders() {
		return adaptedTable.getColumnHeaders();
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		return adaptedTable.getContainerProperty(itemId, propertyId);
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return adaptedTable.getSortableContainerPropertyIds();
	}

	@Override
	public Class<?> getType() {
		Class<?> type = adaptedTable.getType();
		return type != null ? type : super.getType();
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> type = adaptedTable.getType(propertyId);
		if (type == null) {
			type = super.getType(propertyId);
		}
		return type;
	}
}
