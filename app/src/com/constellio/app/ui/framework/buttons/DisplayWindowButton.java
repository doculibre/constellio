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
package com.constellio.app.ui.framework.buttons;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayWindowButton extends WindowButton{
	String text;

	public DisplayWindowButton(String windowCaption,
			String textToDisplay) {
		super("", windowCaption);
		setIcon(DisplayButton.ICON_RESOURCE);
		addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		this.text = textToDisplay;
	}

	@Override
	protected Component buildWindowContent() {
		FormLayout content = new FormLayout();
		Label label = new Label(text);
		label.setContentMode(ContentMode.PREFORMATTED);
		content.addComponent(label);
		content.setSizeUndefined();
		content.setMargin(true);
		return content;
	}

}
