package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
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

import java.util.Collection;

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

		setVisibleColumns(visibleColumns);
		setColumnHeaders(adaptedTable.getColumnHeaders());

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
		Property property = adaptedTable.getContainerProperty(itemId, propertyId);

		if (property != null) {
			Object value = property.getValue();
			if (value instanceof RecordCommentsDisplayImpl) {
				RecordCommentsDisplayImpl recordCommentsDisplayImpl = (RecordCommentsDisplayImpl) value;
				recordCommentsDisplayImpl.setForcedReadOnly(true);
				//recordCommentsDisplayImpl.setReadOnly(true);
			}
		}

		return property;
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
