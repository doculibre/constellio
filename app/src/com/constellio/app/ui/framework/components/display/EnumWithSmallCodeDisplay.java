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
package com.constellio.app.ui.framework.components.display;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.model.entities.EnumWithSmallCode;
import com.vaadin.ui.Label;

public class EnumWithSmallCodeDisplay<T extends EnumWithSmallCode> extends Label {

	@SuppressWarnings("unchecked")
	public EnumWithSmallCodeDisplay(T enumWithSmallCode) {
		if (enumWithSmallCode != null) {
			Class<T> enumWithSmallCodeClass = (Class<T>) enumWithSmallCode.getClass();
			String enumCode = enumWithSmallCode.getCode();
			String caption = $(enumWithSmallCodeClass.getSimpleName() + "." + enumCode);
			setValue(caption);
		}
	}

}
