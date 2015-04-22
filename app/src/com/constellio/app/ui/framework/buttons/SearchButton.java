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

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

public class SearchButton extends Button {
	
	public static final String STYLE_NAME = "search-button";

	public SearchButton() {
		super($("search"));
		init();
	}

	public SearchButton(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_PRIMARY);
	}

}
