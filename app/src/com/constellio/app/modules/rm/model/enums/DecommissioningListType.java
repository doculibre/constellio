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

public enum DecommissioningListType implements EnumWithSmallCode {
	FOLDERS_TO_CLOSE("X"), FOLDERS_TO_TRANSFER("T"), FOLDERS_TO_DEPOSIT("C"), FOLDERS_TO_DESTROY("D");

	private String code;

	DecommissioningListType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public DecommissioningType getDecommissioningType() {
		if (this == FOLDERS_TO_TRANSFER) {
			return DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
		} else if (this == FOLDERS_TO_DEPOSIT) {
			return DecommissioningType.DEPOSIT;
		} else if (this == FOLDERS_TO_DESTROY) {
			return DecommissioningType.DESTRUCTION;
		}
		return null;
	}

	public boolean isClosingOrDestroyal() {
		return this == FOLDERS_TO_CLOSE || this == FOLDERS_TO_DESTROY;
	}

	public boolean isDepositOrDestroyal() {
		return this == FOLDERS_TO_DEPOSIT || this == FOLDERS_TO_DESTROY;
	}
}
