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
package com.constellio.app.services.schemas.bulkImport;

public class ImportError {
	private String invalidElementId;
	private String errorMessage;

	public ImportError(String invalidElementId, String errorMessage) {
		this.invalidElementId = invalidElementId;
		this.errorMessage = errorMessage;
	}

	public String getInvalidElementId() {
		return invalidElementId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getCompleteErrorMessage() {
		return errorMessage + " " + invalidElementId;
	}

	@Override
	public String toString() {
		return getCompleteErrorMessage();
	}
}
