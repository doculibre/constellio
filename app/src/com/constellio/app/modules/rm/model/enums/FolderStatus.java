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
package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FolderStatus implements EnumWithSmallCode {

	ACTIVE("a"), SEMI_ACTIVE("s"), INACTIVE_DESTROYED("d"), INACTIVATE_DEPOSITED("v");

	private String code;

	FolderStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isActive() {
		return this == ACTIVE;
	}

	public boolean isSemiActive() {
		return this == SEMI_ACTIVE;
	}

	public boolean isInactive() {
		return this == INACTIVATE_DEPOSITED || this == INACTIVE_DESTROYED;
	}

	public boolean isDeposited() {
		return this == INACTIVATE_DEPOSITED;
	}

	public boolean isDestroyed() {
		return this == INACTIVE_DESTROYED;
	}

	public boolean isActiveOrSemiActive() {
		return isActive() || isSemiActive();
	}

	public boolean isSemiActiveOrInactive() {
		return isSemiActive() || isInactive();
	}
}
