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

public enum FolderMediaType implements EnumWithSmallCode {

	ANALOG("A"), HYBRID("H"), UNKNOWN("U"), ELECTRONIC("E");

	private String code;

	FolderMediaType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean potentiallyHasAnalogMedium() {
		return this == ANALOG || this == HYBRID || this == UNKNOWN;
	}

	public boolean potentiallyHasElectronicMedium() {
		return this == ELECTRONIC || this == HYBRID || this == UNKNOWN;
	}
}
