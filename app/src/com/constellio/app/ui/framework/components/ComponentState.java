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
package com.constellio.app.ui.framework.components;

public enum ComponentState {
	ENABLED(true, true), DISABLED(true, false), INVISIBLE(false, false);

	private final boolean visible;
	private final boolean enabled;

	ComponentState(boolean visible, boolean enabled) {
		this.visible = visible;
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static ComponentState enabledIf(boolean state) {
		return state ? ENABLED : DISABLED;
	}

	public static ComponentState visibleIf(boolean state) {
		return state ? ENABLED : INVISIBLE;
	}
}
