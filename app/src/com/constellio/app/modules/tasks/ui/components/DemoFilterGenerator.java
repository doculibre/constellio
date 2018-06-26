package com.constellio.app.modules.tasks.ui.components;

import org.joda.time.LocalDate;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.datefilter.DateFilterPopup;
import org.tepi.filtertable.numberfilter.NumberFilterPopup;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Field;

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
    	AbstractField<?> customFilterComponent = null;
		if (propertyId instanceof MetadataVO) {
			MetadataVO metadataVO = (MetadataVO) propertyId;
			Class<?> javaType = metadataVO.getJavaType();
			if (LocalDate.class.isAssignableFrom(javaType)) {
				customFilterComponent = new DateFilterPopup(new DemoFilterDecorator(), propertyId);
			} else if (Number.class.isAssignableFrom(javaType)) {
					customFilterComponent = new NumberFilterPopup(new DemoFilterDecorator());
			} else {
				MetadataFieldFactory factory = new TaskFieldFactory(false);
                final Field<?> field = factory.build(metadataVO);
                if (field != null) {
                    if(field instanceof AbstractTextField) {
                        customFilterComponent = (AbstractTextField) field;
                    } else {
                        customFilterComponent = new FilterWindowButtonField(field);
                    }
                }
            }
		}

        return customFilterComponent;
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
