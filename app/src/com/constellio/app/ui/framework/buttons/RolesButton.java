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

public abstract class RolesButton extends IconButton {
	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/crown.png");
	public static final String BUTTON_STYLE = "roles-button";

	public RolesButton() {
		super(ICON_RESOURCE, $("roles"), true);
		init();
	}

	public RolesButton(boolean iconOnly) {
		super(iconOnly ? ICON_RESOURCE : null, computeCaption($("roles"), iconOnly), iconOnly);
		init();
	}

	public RolesButton(String caption) {
		super(null, computeCaption(caption, false), false);
		init();
	}

	private static String computeCaption(String caption, boolean iconOnly) {
		return iconOnly ? caption : $("roles.icon") + " " + caption;
	}

	private void init() {
		addStyleName(BUTTON_STYLE);
	}

}
