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
public abstract class ManageAuthorizationsButton extends IconButton {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/add_authorization.png");

	public static final String BUTTON_STYLE = "edit-button";

	public ManageAuthorizationsButton() {
		super(ICON_RESOURCE, $("addAuthorization"), true);
		init();
	}

	public ManageAuthorizationsButton(String caption, boolean iconOnly) {
		super(ICON_RESOURCE, caption, iconOnly);
		init();
	}

	public ManageAuthorizationsButton(String caption) {
		super(ICON_RESOURCE, caption);
		init();
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
