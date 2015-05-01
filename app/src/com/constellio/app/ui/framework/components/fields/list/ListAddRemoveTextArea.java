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
package com.constellio.app.ui.framework.components.fields.list;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

@SuppressWarnings("unchecked")
public class ListAddRemoveTextArea extends ListAddRemoveField<String, TextArea> {

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected TextArea newAddEditField() {
		TextArea textArea = new BaseTextArea();
		textArea.setImmediate(false);
		return textArea;
	}

	protected Component newCaptionComponent(String itemId, String caption) {
		caption = StringUtils.replace(caption, "\n", "<br/>");
		Label captionLabel = new Label(caption, ContentMode.HTML);
		return captionLabel;
	}

}
