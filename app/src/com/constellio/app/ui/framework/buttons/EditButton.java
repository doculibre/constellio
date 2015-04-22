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

import static com.constellio.app.ui.i18n.i18n.$;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

@SuppressWarnings("serial")
public abstract class EditButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/modifier.gif");
	
	public static final String BUTTON_STYLE = "edit-button";
	
	public EditButton() {
		super(ICON_RESOURCE, $("edit"), true);
		init();
	}
	
	public EditButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("edit"), iconOnly), iconOnly);
		init();
	}
	
	public EditButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}
	
	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("edit.icon") + " " + caption;
	}
	
	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
