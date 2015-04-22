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
package com.constellio.app.ui.framework.components.fields.date;

import java.util.Date;

import com.constellio.app.ui.framework.components.converters.JodaDateToUtilConverter;
import com.vaadin.data.Property;

public class JodaDateField extends BaseDateField {

	public JodaDateField() {
		super();
		init();
	}

	public JodaDateField(Property<?> dataSource)
			throws IllegalArgumentException {
		super(dataSource);
		init();
	}

	public JodaDateField(String caption, Date value) {
		super(caption, value);
		init();
	}

	public JodaDateField(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public JodaDateField(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		setConverter(new JodaDateToUtilConverter());
	}

}
