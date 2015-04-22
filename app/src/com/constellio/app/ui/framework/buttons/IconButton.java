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

import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class IconButton extends BaseButton {
	public IconButton(Resource iconResource, String caption) {
		this(iconResource, caption, iconResource != null);
	}

	public IconButton(Resource iconResource, String caption, boolean iconOnly) {
		super(caption);
		setIcon(iconResource);
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		setCaption(caption);

		if (iconOnly) {
			addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			setIconAlternateText(caption);
		}
	}

}
