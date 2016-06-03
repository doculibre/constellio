package com.constellio.app.ui.framework.components.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.Container;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.OptionGroup;

/**
 * @author Vincent
 */
public class ListOptionGroup extends OptionGroup {

	public ListOptionGroup() {
		init();
	}

	public ListOptionGroup(String caption) {
		super(caption);
		init();
	}

	public ListOptionGroup(String caption, Collection<?> options) {
		super(caption, options);
		init();
	}

	public ListOptionGroup(String caption, Container dataSource) {
		super(caption, dataSource);
		init();
	}
	
	private void init() {
		setConverter(new ObjectToCollectionConverter());
	}

    /**
     * Returns the type of the property. <code>getValue</code> and
     * <code>setValue</code> methods must be compatible with this type: one can
     * safely cast <code>getValue</code> to given type and pass any variable
     * assignable to this type as a parameter to <code>setValue</code>.
     * 
     * @return the Type of the property.
     */
    @Override
    public Class<?> getType() {
        if (isMultiSelect()) {
            return List.class;
        } else {
            return Object.class;
        }
    }
	
	private static class ObjectToCollectionConverter implements Converter<Object, Collection<?>> {

		@Override
		public List<?> convertToModel(Object value, Class<? extends Collection<?>> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			List<?> model;
			if (value instanceof List) {
				model = (List<?>) value;
			} else if (value instanceof Collection) {
				model = new ArrayList<Object>((Collection<?>) value);
			} else {
				model = new ArrayList<Object>();
			}
			return model;
		}

		@Override
		public Object convertToPresentation(Collection<?> value, Class<? extends Object> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return value;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getModelType() {
			return Collection.class;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getPresentationType() {
			return Object.class;
		}
		
	}

}
