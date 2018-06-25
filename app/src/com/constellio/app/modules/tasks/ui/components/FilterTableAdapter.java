package com.constellio.app.modules.tasks.ui.components;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.tepi.filtertable.FilterTable;

import java.util.Collection;

public class FilterTableAdapter extends FilterTable {
    private final Table adaptedTable;

    public FilterTableAdapter(Table adaptedTable) {
        this.adaptedTable = adaptedTable;

        Collection<?> listeners = adaptedTable.getListeners(AttachEvent.class);
        for(Object al: CollectionUtils.emptyIfNull(listeners)) {
            ((AttachListener)al).attach(new AttachEvent(this));
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
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addColumnCollapseListener(new ColumnCollapseListenerAdapter((Table.ColumnCollapseListener) tccl));
        }

        listeners = adaptedTable.getListeners(Table.ColumnReorderEvent.class);
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addColumnReorderListener(new ColumnReorderListenerAdapter((Table.ColumnReorderListener) tccl));
        }

        listeners = adaptedTable.getListeners(ItemClickEvent.class);
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addItemClickListener((ItemClickEvent.ItemClickListener) tccl);
        }

        setCellStyleGenerator(new CellStyleGeneratorAdapter(adaptedTable, adaptedTable.getCellStyleGenerator()));

        addStyleName(adaptedTable.getStyleName());

        resetFilters();

        setSizeFull();
    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public ColumnGenerator getColumnGenerator(Object columnId) throws IllegalArgumentException {
        Table.ColumnGenerator columnGenerator = adaptedTable.getColumnGenerator(columnId);
        if(columnGenerator instanceof CustomTable.ColumnGenerator) {
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
    public void containerItemSetChange(Container.ItemSetChangeEvent event) {
        adaptedTable.containerItemSetChange(event);

        super.containerItemSetChange(event);
    }

    @Override
    public Class<?> getType() {
        Class<?> type = adaptedTable.getType();
        return type != null ? type : super.getType();
    }

    @Override
    public Class<?> getType(Object propertyId) {
        Class<?> type = adaptedTable.getType(propertyId);
        if(type == null) {
            type = super.getType(propertyId);
        }
        return type;
    }
}
