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

public class TaskFilteringTable extends FilterTable {
    private final TaskTable taskTable;

    public TaskFilteringTable(TaskTable taskTable) {
        this.taskTable = taskTable;

        Collection<?> listeners = taskTable.getListeners(AttachEvent.class);
        for(Object al: CollectionUtils.emptyIfNull(listeners)) {
            ((AttachListener)al).attach(new AttachEvent(this));
        }

        setContainerDataSource(taskTable.getContainerDataSource());

        Object[] visibleColumns = taskTable.getVisibleColumns();
        for (Object propertyId : ArrayUtils.nullToEmpty(visibleColumns)) {
            addGeneratedColumn(propertyId.toString(), new ColumnGeneratorAdapter(taskTable, taskTable.getColumnGenerator(propertyId.toString())));
        }

        setVisibleColumns(visibleColumns);
        setColumnHeaders(taskTable.getColumnHeaders());

        setColumnReorderingAllowed(taskTable.isColumnReorderingAllowed());
        setColumnCollapsingAllowed(taskTable.isColumnCollapsingAllowed());

        Collection<?> propertyIds = taskTable.getContainerPropertyIds();
        for (Object propertyId : CollectionUtils.emptyIfNull(propertyIds)) {
            setColumnCollapsed(propertyId, taskTable.isColumnCollapsed(propertyId));
        }

        listeners = taskTable.getListeners(Table.ColumnCollapseEvent.class);
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addColumnCollapseListener(new ColumnCollapseListenerAdapter((Table.ColumnCollapseListener) tccl));
        }

        listeners = taskTable.getListeners(Table.ColumnReorderEvent.class);
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addColumnReorderListener(new ColumnReorderListenerAdapter((Table.ColumnReorderListener) tccl));
        }

        listeners = taskTable.getListeners(ItemClickEvent.class);
        for(Object tccl: CollectionUtils.emptyIfNull(listeners)) {
            addItemClickListener((ItemClickEvent.ItemClickListener) tccl);
        }

        setCellStyleGenerator(new CellStyleGeneratorAdapter(taskTable, taskTable.getCellStyleGenerator()));

        addStyleName(taskTable.getStyleName());

        resetFilters();

        setSizeFull();
    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public ColumnGenerator getColumnGenerator(Object columnId) throws IllegalArgumentException {
        Table.ColumnGenerator columnGenerator = taskTable.getColumnGenerator(columnId);
        if(columnGenerator instanceof CustomTable.ColumnGenerator) {
            return (ColumnGenerator) columnGenerator;
        }

        return super.getColumnGenerator(columnId);
    }

    @Override
    public Container getContainerDataSource() {
        return taskTable.getContainerDataSource();
    }

    @Override
    public String[] getColumnHeaders() {
        return taskTable.getColumnHeaders();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return taskTable.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        return taskTable.getSortableContainerPropertyIds();
    }

    @Override
    public void containerItemSetChange(Container.ItemSetChangeEvent event) {
        taskTable.containerItemSetChange(event);

        super.containerItemSetChange(event);
    }

    @Override
    public Class<?> getType() {
        Class<?> type = taskTable.getType();
        return type != null ? type : super.getType();
    }

    @Override
    public Class<?> getType(Object propertyId) {
        Class<?> type = taskTable.getType(propertyId);
        if(type == null) {
            type = super.getType(propertyId);
        }
        return type;
    }
}
