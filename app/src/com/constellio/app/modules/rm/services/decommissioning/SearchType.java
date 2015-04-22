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
package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.model.enums.DecommissioningListType;

public enum SearchType {
	fixedPeriod, code888, code999, transfer, activeToDeposit, activeToDestroy, semiActiveToDeposit, semiActiveToDestroy;

	public DecommissioningListType toDecomListType() {
		switch (this) {
		case fixedPeriod:
		case code888:
		case code999:
			return DecommissioningListType.FOLDERS_TO_CLOSE;
		case transfer:
			return DecommissioningListType.FOLDERS_TO_TRANSFER;
		case activeToDeposit:
		case semiActiveToDeposit:
			return DecommissioningListType.FOLDERS_TO_DEPOSIT;
		case activeToDestroy:
		case semiActiveToDestroy:
			return DecommissioningListType.FOLDERS_TO_DESTROY;
		}
		return null;
	}

	public boolean isFromSemiActive() {
		return this == semiActiveToDeposit || this == semiActiveToDestroy;
	}
}
