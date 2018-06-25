package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import org.tepi.filtertable.FilterGenerator;

public class DemoFilterGenerator implements FilterGenerator {

    @Override
    public Container.Filter generateFilter(final Object propertyId, final Object value) {
        if(propertyId instanceof MetadataVO) {
            return new RecordVOFilter((MetadataVO) propertyId, value);
        }

        // For other properties, use the default filter
        return null;
    }

    @Override
    public Container.Filter generateFilter(Object propertyId, Field<?> originatingField) {
        Object value = originatingField.getValue();
        return generateFilter(propertyId, value);
    }

    @Override
    public AbstractField<?> getCustomFilterComponent(Object propertyId) {
        return null;
    }

    @Override
    public void filterRemoved(Object propertyId) {
    }

    @Override
    public void filterAdded(Object propertyId, Class<? extends Container.Filter> filterType, Object value) {
    }

    @Override
    public Container.Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
        return null;
    }
}
