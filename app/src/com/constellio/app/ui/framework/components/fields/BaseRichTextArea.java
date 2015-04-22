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

import com.vaadin.data.Property;
import com.vaadin.ui.RichTextArea;

public class BaseRichTextArea extends RichTextArea {

	public BaseRichTextArea() {
		init();
	}

	public BaseRichTextArea(String caption) {
		super(caption);
		init();
	}

	public BaseRichTextArea(Property<?> dataSource) {
		super(dataSource);
		init();
	}

	public BaseRichTextArea(String caption, Property<?> dataSource) {
		super(caption, dataSource);
		init();
	}

	public BaseRichTextArea(String caption, String value) {
		super(caption, value);
		init();
	}
	
	private void init() {
		setNullRepresentation("");
		setWidth("100%");
	}

}
