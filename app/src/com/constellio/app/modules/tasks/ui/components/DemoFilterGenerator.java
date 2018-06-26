package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.rm.ui.components.retentionRule.FolderCopyRetentionRuleTable;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.datefilter.DateFilterPopup;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.vaadin.data.Container;

import static com.constellio.app.ui.i18n.i18n.$;

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
			} else {
				MetadataFieldFactory factory = new MetadataFieldFactory();
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
