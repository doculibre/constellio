/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		setConverter(new ObjectToListConverter());
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
	
	private static class ObjectToListConverter implements Converter<Object, List<?>> {

		@Override
		public List<?> convertToModel(Object value, Class<? extends List<?>> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			List<?> model;
			if (value instanceof List) {
				model = (List<?>) value;
			} else if (value instanceof Collection) {
				model = new ArrayList<Object>((Collection<?>) value);
			} else {
				model = null;
			}
			return model;
		}

		@Override
		public Object convertToPresentation(List<?> value, Class<? extends Object> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return value;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getModelType() {
			return List.class;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Class getPresentationType() {
			return Object.class;
		}
		
	}

}
