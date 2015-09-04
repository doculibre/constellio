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
package com.constellio.app.ui.framework.components.fields.number;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToDoubleConverter;

public class BaseDoubleField extends BaseTextField {

	public BaseDoubleField() {
		init();
	}

	public BaseDoubleField(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseDoubleField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseDoubleField(String caption, String value) {
		super(caption, value);
		init();
	}

	public BaseDoubleField(String caption) {
		super(caption);
		init();
	}

	@Override
	public Double getConvertedValue() {
		return (Double) super.getConvertedValue();
	}

	private void init() {
		setConverter(new StringToDoubleConverter());
		setWidth("100px");
	}
}
